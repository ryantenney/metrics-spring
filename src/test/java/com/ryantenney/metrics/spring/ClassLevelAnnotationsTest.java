package com.ryantenney.metrics.spring;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.annotation.ExceptionMetered;
import com.codahale.metrics.annotation.Metered;
import com.codahale.metrics.annotation.Timed;
import com.ryantenney.metrics.spring.config.annotation.EnableMetrics;
import com.ryantenney.metrics.spring.config.annotation.MetricsConfigurerAdapter;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;

/**
 * Created by david on 06/05/15.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class ClassLevelAnnotationsTest extends Assert {

    @Configuration
    @EnableMetrics( proxyTargetClass = true )
    public static class TestConfiguration extends MetricsConfigurerAdapter {

        @Bean
        public TimedClass timedClass() {
            return new TimedClass();
        }

        @Bean
        public ExceptionMeteredClass exceptionMetered() {
            return new ExceptionMeteredClass();
        }

        @Bean
        public MeteredClass meteredClass() {
            return new MeteredClass();
        }

        @Bean
        @Override
        public MetricRegistry getMetricRegistry() {
            return new MetricRegistry();

        }
    }

    @Inject
    private TimedClass timedClass;

    @Inject
    private ExceptionMeteredClass exceptionMeteredClass;

    @Inject
    private MetricRegistry metricRegistry;

    @Inject
    private MeteredClass meteredClass;

    @Test
    public void testTimedIsIntercepted() {
        timedClass.someMethod();
        assertEquals(1, metricRegistry.getTimers().size());
    }

    @Test
    public void testMeteredIsIntercepted() {
        meteredClass.someMeteredMethod();
        assertEquals(1, metricRegistry.getMeters().size());
    }

    @Test
    public void testExceptionIsIntercepted() {
        try {
            exceptionMeteredClass.someExceptionMeteredMethod();
        }
        catch (RuntimeException e) {}

        assertEquals(2, metricRegistry.getMetrics().size());
    }

    @Timed
    public static class TimedClass {
        public void someMethod() {}
    }

    @ExceptionMetered
    public static class ExceptionMeteredClass {
        public void someExceptionMeteredMethod() {
            throw new RuntimeException();
        }
    }

    @Metered
    public static class MeteredClass {
        public void someMeteredMethod(){}
    }
}
