package com.ryantenney.metrics.spring.benchmarks;

import com.google.caliper.Runner;
import com.google.caliper.SimpleBenchmark;
import com.ryantenney.metrics.spring.config.annotation.EnableMetrics;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.annotation.Metered;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Random;

public class MeterBenchmark extends SimpleBenchmark {

    public static void main(String[] args) throws Exception {
        new Runner().run(MeterBenchmark.class.getName());
    }

    private AnnotationConfigApplicationContext ctx;
    private BenchmarkTarget targetBean;

    private Meter meter;
    private BenchmarkTarget targetObj;

    @Override
    protected void setUp() throws Exception {
        ctx = new AnnotationConfigApplicationContext(BenchmarkConfig.class);
        ctx.start();
        targetBean = ctx.getBean(BenchmarkTarget.class);

        meter = new MetricRegistry().meter("foobar");
        targetObj = new BenchmarkTarget();
    }

    @Override
    protected void tearDown() throws Exception {
        ctx.stop();
    }

    @SuppressWarnings("unused")
    public void timeBean(int reps) {
        for (int i = 0; i < reps; i++) {
            targetBean.mark();
        }
    }

    @SuppressWarnings("unused")
    public void timeNormal(int reps) {
        for (int i = 0; i < reps; i++) {
            meter.mark();
            targetObj.mark();
        }
    }

    @Configuration
    @EnableMetrics
    public static class BenchmarkConfig {

        @Bean
        public BenchmarkTarget createTarget() {
            return new BenchmarkTarget();
        }

    }

    public static class BenchmarkTarget {
        @Metered
        public Object mark() {
            return null;
        }
    }

}
