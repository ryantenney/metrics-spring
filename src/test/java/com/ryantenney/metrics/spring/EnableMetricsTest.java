/*
 * Copyright 2012 Ryan W Tenney (http://ryan.10e.us)
 *            and Martello Technologies (http://martellotech.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ryantenney.metrics.spring;

import com.yammer.metrics.*;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.ryantenney.metrics.spring.config.annotation.EnableMetrics;
import com.ryantenney.metrics.spring.config.annotation.MetricsConfigurer;
import com.yammer.metrics.annotation.ExceptionMetered;
import com.yammer.metrics.annotation.Metered;
import com.yammer.metrics.annotation.Timed;
import com.yammer.metrics.health.HealthCheckRegistry;

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

	private static final Logger log = LoggerFactory.getLogger(EnableMetricsTest.class);

	private static final MetricRegistry metricRegistry = new MetricRegistry();
	private static final HealthCheckRegistry healthCheckRegistry = new HealthCheckRegistry();

	static {
		metricRegistry.addListener(new LoggingMetricRegistryListener());
	}

	@Test
	public void metricsConfigTest() throws Throwable {
		// Initialize the context
		AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext();
		applicationContext.register(MetricsConfig.class);
		applicationContext.refresh();

		// Assert that the custom registries were used
		assertSame(metricRegistry, applicationContext.getBean(MetricRegistry.class));
		assertSame(healthCheckRegistry, applicationContext.getBean(HealthCheckRegistry.class));

		// Verify that the configureReporters method was invoked
		assertThat(MetricsConfig.isConfigureReportersInvoked, is(true));

		// Assert that the bean has been proxied
		TestBean testBean = applicationContext.getBean(TestBean.class);
        assertNotNull(testBean);
		assertThat(AopUtils.isAopProxy(testBean), is(true));

		// Verify that the Gauge field's value is returned
		Gauge<Integer> fieldGauge = (Gauge<Integer>) forGaugeField(metricRegistry, TestBean.class, "intGaugeField");
        assertNotNull(fieldGauge);
		assertThat(fieldGauge.getValue(), is(5));

		// Verify that the Gauge method's value is returned
		Gauge<Integer> methodGauge = (Gauge<Integer>) forGaugeMethod(metricRegistry, TestBean.class, "intGaugeMethod");
        assertNotNull(methodGauge);
		assertThat(methodGauge.getValue(), is(6));

		// Verify that the Timer's counter is incremented on method invocation
		Timer timedMethodTimer = forTimedMethod(metricRegistry, TestBean.class, "timedMethod");
        assertNotNull(timedMethodTimer);
		assertThat(timedMethodTimer.getCount(), is(0L));
		testBean.timedMethod();
		assertThat(timedMethodTimer.getCount(), is(1L));

		// Verify that the Meter's counter is incremented on method invocation
		Meter meteredMethodMeter = forMeteredMethod(metricRegistry, TestBean.class, "meteredMethod");
        assertNotNull(meteredMethodMeter);
		assertThat(meteredMethodMeter.getCount(), is(0L));
		testBean.meteredMethod();
		assertThat(meteredMethodMeter.getCount(), is(1L));

		// Verify that the Meter's counter is incremented on method invocation
		Meter exceptionMeteredMethodMeter = forExceptionMeteredMethod(metricRegistry, TestBean.class, "exceptionMeteredMethod");
        assertNotNull(exceptionMeteredMethodMeter);
		assertThat(exceptionMeteredMethodMeter.getCount(), is(0L));
		try {
			testBean.exceptionMeteredMethod();
		} catch (Throwable t) {}
		assertThat(exceptionMeteredMethodMeter.getCount(), is(1L));
	}

	@Configuration
	@EnableMetrics
	public static class MetricsConfig implements MetricsConfigurer {

		public static boolean isConfigureReportersInvoked = false;

		@Bean
		public TestBean testBean() {
			return new TestBean();
		}

		@Override
		public MetricRegistry getMetricRegistry() {
			return metricRegistry;
		}

		@Override
		public HealthCheckRegistry getHealthCheckRegistry() {
			return healthCheckRegistry;
		}

		@Override
		public void configureReporters(MetricRegistry metricRegistry) {
			isConfigureReportersInvoked = true;
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
