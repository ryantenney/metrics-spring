/**
 * Copyright © 2012 Ryan W Tenney (ryan@10e.us)
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

import static com.ryantenney.metrics.spring.TestUtil.forCachedGaugeMethod;
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

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.aop.support.AopUtils;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.codahale.metrics.CachedGauge;
import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.codahale.metrics.annotation.Counted;
import com.codahale.metrics.annotation.ExceptionMetered;
import com.codahale.metrics.annotation.Metered;
import com.codahale.metrics.annotation.Timed;
import com.codahale.metrics.health.HealthCheckRegistry;
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

	private static AnnotationConfigApplicationContext applicationContext;
	private static MetricRegistry metricRegistry;
	private static HealthCheckRegistry healthCheckRegistry;
	private static TestBean testBean;

	@BeforeClass
	public static void beforeClass() {
		metricRegistry = new MetricRegistry();
		metricRegistry.addListener(new LoggingMetricRegistryListener());

		healthCheckRegistry = new HealthCheckRegistry();

		applicationContext = new AnnotationConfigApplicationContext(MetricsConfig.class);
		testBean = applicationContext.getBean(TestBean.class);
	}

	@AfterClass
	public static void afterClass() {
		if (applicationContext != null) {
			applicationContext.close();
		}
	}

	@Test
	public void customRegistries() throws Throwable {
		// Assert that the custom registries were used
		assertSame(metricRegistry, applicationContext.getBean(MetricRegistry.class));
		assertSame(healthCheckRegistry, applicationContext.getBean(HealthCheckRegistry.class));
	}

	@Test
	public void configureReportersInvoked() throws Throwable {
		// Verify that the configureReporters method was invoked
		assertThat(MetricsConfig.isConfigureReportersInvoked, is(true));
	}

	@Test
	public void beanIsProxied() throws Throwable {
		// Assert that the bean has been proxied
		TestBean testBean = applicationContext.getBean(TestBean.class);
		assertNotNull(testBean);
		assertThat(AopUtils.isAopProxy(testBean), is(true));
	}

	@Test
	public void gaugeField() throws Throwable {
		// Verify that the Gauge field's value is returned
		Gauge<Integer> fieldGauge = (Gauge<Integer>) forGaugeField(metricRegistry, TestBean.class, "intGaugeField");
		assertNotNull(fieldGauge);
		assertThat(fieldGauge.getValue(), is(5));
	}

	@Test
	public void gaugeMethod() throws Throwable {
		// Verify that the Gauge method's value is returned
		Gauge<Integer> methodGauge = (Gauge<Integer>) forGaugeMethod(metricRegistry, TestBean.class, "intGaugeMethod");
		assertNotNull(methodGauge);
		assertThat(methodGauge.getValue(), is(6));
	}

	@Test
	public void cachedGaugeMethod() throws Throwable {
		// Verify that the Gauge method's value is returned
		CachedGauge<Integer> methodCachedGauge = (CachedGauge<Integer>) forCachedGaugeMethod(metricRegistry, TestBean.class, "cachedGaugeMethod");
		assertNotNull(methodCachedGauge);
		assertThat(methodCachedGauge.getValue(), is(7));
	}

	@Test
	public void timedMethod() throws Throwable {
		// Verify that the Timer's counter is incremented on method invocation
		Timer timedMethodTimer = forTimedMethod(metricRegistry, TestBean.class, "timedMethod");
		assertNotNull(timedMethodTimer);
		assertThat(timedMethodTimer.getCount(), is(0L));
		testBean.timedMethod();
		assertThat(timedMethodTimer.getCount(), is(1L));
	}

	@Test
	public void meteredMethod() throws Throwable {
		// Verify that the Meter's counter is incremented on method invocation
		Meter meteredMethodMeter = forMeteredMethod(metricRegistry, TestBean.class, "meteredMethod");
		assertNotNull(meteredMethodMeter);
		assertThat(meteredMethodMeter.getCount(), is(0L));
		testBean.meteredMethod();
		assertThat(meteredMethodMeter.getCount(), is(1L));
	}

	@Test
	public void countedMethod() throws Throwable {
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
	}

	@Test
	public void exceptionMeteredMethod() throws Throwable {
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

		@com.codahale.metrics.annotation.CachedGauge(timeout = 100)
		public int cachedGaugeMethod() {
			return 7;
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
