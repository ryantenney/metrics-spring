/**
 * Copyright (C) 2012 Ryan W Tenney (ryan@10e.us)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ryantenney.metrics.spring;

import static com.ryantenney.metrics.spring.TestUtil.forCountedMethod;
import static com.ryantenney.metrics.spring.TestUtil.forExceptionMeteredMethod;
import static com.ryantenney.metrics.spring.TestUtil.forGaugeField;
import static com.ryantenney.metrics.spring.TestUtil.forGaugeMethod;
import static com.ryantenney.metrics.spring.TestUtil.forMeteredMethod;
import static com.ryantenney.metrics.spring.TestUtil.forTimedMethod;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.springframework.aop.support.AopUtils;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.codahale.metrics.annotation.ExceptionMetered;
import com.codahale.metrics.annotation.Metered;
import com.codahale.metrics.annotation.Timed;
import com.codahale.metrics.health.HealthCheckRegistry;
import com.ryantenney.metrics.annotation.Counted;
import com.ryantenney.metrics.spring.config.annotation.EnableMetrics;
import com.ryantenney.metrics.spring.config.annotation.MetricsConfigurer;

/**
 * Tests use of {@link EnableMetrics @EnableMetrics} on {@code @Configuration} classes.
 * 
 * @author Ryan Tenney
 * @since 3.0
 */
@SuppressWarnings("unchecked")
public class EnableMetricsTest {

	private static final MetricRegistry metricRegistry = new MetricRegistry();
	private static final HealthCheckRegistry healthCheckRegistry = new HealthCheckRegistry();

	static {
		metricRegistry.addListener(new LoggingMetricRegistryListener());
	}

	@Test
	public void metricsConfigTest() throws Throwable {
		// Initialize the context
		AnnotationConfigApplicationContext applicationContext = null;
		try {
			applicationContext = new AnnotationConfigApplicationContext();
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
			final Counter countedMethodMeter = forCountedMethod(metricRegistry, TestBean.class, "countedMethod");
			assertNotNull(countedMethodMeter);
			assertThat(countedMethodMeter.getCount(), is(0L));
			testBean.countedMethod(new Runnable() {
				@Override
				public void run() {
					assertThat(countedMethodMeter.getCount(), is(1L));
				}
			});
			assertThat(countedMethodMeter.getCount(), is(0L));
	
			// Verify that the Meter's counter is incremented on method invocation
			Meter exceptionMeteredMethodMeter = forExceptionMeteredMethod(metricRegistry, TestBean.class, "exceptionMeteredMethod");
			assertNotNull(exceptionMeteredMethodMeter);
			assertThat(exceptionMeteredMethodMeter.getCount(), is(0L));
			try {
				testBean.exceptionMeteredMethod();
			}
			catch (Throwable t) {}
			assertThat(exceptionMeteredMethodMeter.getCount(), is(1L));
		}
		finally {
			if (applicationContext != null) {
				applicationContext.close();
			}
		}
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

		@com.codahale.metrics.annotation.Gauge
		private int intGaugeField = 5;

		@com.codahale.metrics.annotation.Gauge
		public int intGaugeMethod() {
			return 6;
		}

		@Timed
		public void timedMethod() {}

		@Metered
		public void meteredMethod() {}

		@Counted
		public void countedMethod(Runnable runnable) {
			if (runnable != null) runnable.run();
		}

		@ExceptionMetered
		public void exceptionMeteredMethod() {
			throw new RuntimeException();
		}

	}

}
