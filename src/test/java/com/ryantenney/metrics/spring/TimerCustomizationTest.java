package com.ryantenney.metrics.spring;

import static org.junit.Assert.*;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.aop.framework.ProxyConfig;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.ryantenney.metrics.spring.config.annotation.EnableMetrics;
import com.ryantenney.metrics.spring.config.annotation.MetricsConfigurer;

import io.dropwizard.metrics.MetricName;
import io.dropwizard.metrics.MetricRegistry;
import io.dropwizard.metrics.SlidingTimeWindowReservoir;
import io.dropwizard.metrics.Timer;
import io.dropwizard.metrics.annotation.Timed;
import io.dropwizard.metrics.health.HealthCheckRegistry;

public class TimerCustomizationTest {

	private static AnnotationConfigApplicationContext applicationContext;
	private static MetricRegistry metricRegistry;
	private static HealthCheckRegistry healthCheckRegistry;
	private static Timer defaultAnnotationTimer;
	private static Timer customAnnotationTimer;


	@BeforeClass
	public static void beforeClass() {
		metricRegistry = new MetricRegistry();
		metricRegistry.addListener(new LoggingMetricRegistryListener());

		healthCheckRegistry = new HealthCheckRegistry();
		
		defaultAnnotationTimer =new Timer(new SlidingTimeWindowReservoir(30, TimeUnit.MINUTES));
		customAnnotationTimer =new Timer(new SlidingTimeWindowReservoir(1, TimeUnit.MINUTES));

		applicationContext = new AnnotationConfigApplicationContext(MetricsConfig.class);
	}

	@AfterClass
	public static void afterClass() {
		if (applicationContext != null) {
			applicationContext.close();
		}
	}


	@Test
	public void testDefaultTimedAnnotation() {
		Timer timer = metricRegistry.getTimers().get(new MetricName("customTimedName.timedMethod"));
		assertNotNull(timer);
		assertSame(defaultAnnotationTimer, timer);
	}

	@Test
	public void testCustomTimedAnnotation() {
		Timer timer = metricRegistry.getTimers().get(new MetricName("customTimedAnnotation.customTimedMethod"));
		assertNotNull(timer);
		assertSame(customAnnotationTimer, timer);
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

		// Configure a CustomTimed annotation
		@Bean	
		public BeanPostProcessor myTimedAnnotationBeanPostProcessor(MetricRegistry metricRegistry) {
			ProxyConfig proxyConfig = new ProxyConfig();
			MetricFactory<Timer, CustomTimed> timerFactory = new MetricFactory<Timer, CustomTimed>() {
			
				@Override
				public Timer getMetric(MetricRegistry metricRegistry, MetricName metricName, CustomTimed annotation) {
					return metricRegistry.register(metricName, customAnnotationTimer);
				}
			};
			MetricNamingStrategy<CustomTimed> namingStrategy = new MetricNamingStrategy<CustomTimed>() {
				@Override
				public MetricName buildMetricName(Class<?> targetClass, Method method, CustomTimed annotation) {
					return new MetricName("customTimedAnnotation."+method.getName());
				}
			};
			return MetricsBeanPostProcessorFactory.timer(metricRegistry, proxyConfig, CustomTimed.class, timerFactory, namingStrategy);
		}

		// Customize the naming strategy for the default @Timed annotation
		@Bean
		public MetricNamingStrategy<Timed> timedNamingStrategy() {
			return new MetricNamingStrategy<Timed>() {
				@Override
				public MetricName buildMetricName(Class<?> targetClass, Method method, Timed annotation) {
					return new MetricName("customTimedName."+method.getName());
				}
			};
		}
		
		// Customize the timer construction for the default @Timed annotation
		@Bean
		public MetricFactory<Timer, Timed> timedFactory() {
			return new MetricFactory<Timer, Timed>() {

				@Override
				public Timer getMetric(MetricRegistry metricRegistry, MetricName metricName, Timed annotation) {
					return metricRegistry.register(metricName, defaultAnnotationTimer);
				}
			};
		}
	}

	public static class TestBean {

		@Timed
		public void timedMethod() {}
		
		@CustomTimed
		public void customTimedMethod() {}
	}
	
	@Retention(RetentionPolicy.RUNTIME)
	@Target({ ElementType.TYPE, ElementType.CONSTRUCTOR, ElementType.METHOD, ElementType.ANNOTATION_TYPE })
	public static @interface CustomTimed {

	}
}
