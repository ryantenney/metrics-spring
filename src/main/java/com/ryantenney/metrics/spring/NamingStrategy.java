package com.ryantenney.metrics.spring;

import com.codahale.metrics.annotation.ExceptionMetered;
import com.codahale.metrics.annotation.Gauge;
import com.codahale.metrics.annotation.Metered;
import com.codahale.metrics.annotation.Timed;
import com.ryantenney.metrics.annotation.CachedGauge;
import com.ryantenney.metrics.annotation.Counted;
import com.ryantenney.metrics.annotation.InjectMetric;

import java.lang.reflect.Member;

/**
 * @author orantius
 * @version $Id$
 * @since 12/8/13
 */
public interface NamingStrategy {
    String forTimedMethod(Class<?> klass, String beanName, Member member, Timed annotation);

    String forMeteredMethod(Class<?> klass, String beanName, Member member, Metered annotation);

    String forGauge(Class<?> klass, String beanName, Member member, Gauge annotation);

    String forCachedGauge(Class<?> klass, String beanName, Member member, CachedGauge annotation);

    String forExceptionMeteredMethod(Class<?> klass, String beanName, Member member, ExceptionMetered annotation);

    String forCountedMethod(Class<?> klass, String beanName, Member member, Counted annotation);

    String forInjectMetricField(Class<?> klass, String beanName, Member member, InjectMetric annotation);
}
