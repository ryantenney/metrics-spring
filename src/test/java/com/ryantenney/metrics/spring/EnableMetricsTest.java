package com.ryantenney.metrics.spring;

import org.junit.Test;
import org.springframework.aop.support.AopUtils;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.ryantenney.metrics.spring.config.annotation.EnableMetrics;
import com.ryantenney.metrics.spring.config.annotation.MetricsConfigurer;
import com.yammer.metrics.annotation.ExceptionMetered;
import com.yammer.metrics.annotation.Metered;
import com.yammer.metrics.annotation.Timed;
import com.yammer.metrics.core.Gauge;
import com.yammer.metrics.core.HealthCheckRegistry;
import com.yammer.metrics.core.Meter;
import com.yammer.metrics.core.MetricsRegistry;
import com.yammer.metrics.core.Timer;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static com.ryantenney.metrics.spring.TestUtil.*;

/**
 * Tests use of {@link EnableMetrics @EnableMetrics} on {@code @Configuration} classes.
 * 
 * @author Ryan Tenney
 * @since 3.0
 */
@SuppressWarnings("unchecked")
public class EnableMetricsTest {

	private static final MetricsRegistry metricsRegistry = new MetricsRegistry();
	private static final HealthCheckRegistry healthCheckRegistry = new HealthCheckRegistry();

	@Test
	public void metricsConfigTest() throws Throwable {
		// Initialize the context
		AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext();
		applicationContext.register(MetricsConfig.class);
		applicationContext.refresh();

		// Assert that the custom registries were used
		assertSame(metricsRegistry, applicationContext.getBean(MetricsRegistry.class));
		assertSame(healthCheckRegistry, applicationContext.getBean(HealthCheckRegistry.class));

		// Verify that the configureMetricsReporters method was invoked
		assertThat(MetricsConfig.isConfigureMetricsReportersInvoked, is(true));

		// Assert that the bean has been proxied
		TestBean testBean = applicationContext.getBean(TestBean.class);
		assertThat(AopUtils.isAopProxy(testBean), is(true));

		// Verify that the Gauge field's value is returned
		Gauge<Integer> fieldGauge = (Gauge<Integer>) forGaugeField(metricsRegistry, TestBean.class, "intGaugeField");
		assertThat(fieldGauge.getValue(), is(5));

		// Verify that the Gauge method's value is returned
		Gauge<Integer> methodGauge = (Gauge<Integer>) forGaugeMethod(metricsRegistry, TestBean.class, "intGaugeMethod");
		assertThat(methodGauge.getValue(), is(6));

		// Verify that the Timer's counter is incremented on method invocation
		Timer timedMethodTimer = forTimedMethod(metricsRegistry, TestBean.class, "timedMethod");
		assertThat(timedMethodTimer.getCount(), is(0L));
		testBean.timedMethod();
		assertThat(timedMethodTimer.getCount(), is(1L));

		// Verify that the Meter's counter is incremented on method invocation
		Meter meteredMethodMeter = forMeteredMethod(metricsRegistry, TestBean.class, "meteredMethod");
		assertThat(meteredMethodMeter.getCount(), is(0L));
		testBean.meteredMethod();
		assertThat(meteredMethodMeter.getCount(), is(1L));

		// Verify that the Meter's counter is incremented on method invocation
		Meter exceptionMeteredMethodMeter = forExceptionMeteredMethod(metricsRegistry, TestBean.class, "exceptionMeteredMethod");
		assertThat(exceptionMeteredMethodMeter.getCount(), is(0L));
		try {
			testBean.exceptionMeteredMethod();
		} catch (Throwable t) {}
		assertThat(exceptionMeteredMethodMeter.getCount(), is(1L));
	}

	@Configuration
	@EnableMetrics
	public static class MetricsConfig implements MetricsConfigurer {

		public static boolean isConfigureMetricsReportersInvoked = false;

		@Bean
		public TestBean testBean() {
			return new TestBean();
		}

		@Override
		public MetricsRegistry getMetricsRegistry() {
			return metricsRegistry;
		}

		@Override
		public HealthCheckRegistry getHealthCheckRegistry() {
			return healthCheckRegistry;
		}

		@Override
		public void configureMetricsReporters(MetricsRegistry metricsRegistry) {
			isConfigureMetricsReportersInvoked = true;
		}

	}

	public static class TestBean {

		@com.yammer.metrics.annotation.Gauge
		private int intGaugeField = 5;

		@com.yammer.metrics.annotation.Gauge
		public int intGaugeMethod() {
			return 6;
		}

		@Timed
		public void timedMethod() {}

		@Metered
		public void meteredMethod() {}

		@ExceptionMetered
		public void exceptionMeteredMethod() {
			throw new RuntimeException();
		}

	}

}
