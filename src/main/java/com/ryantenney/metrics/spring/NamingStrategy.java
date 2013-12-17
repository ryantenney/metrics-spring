package com.ryantenney.metrics.spring;

import com.codahale.metrics.annotation.ExceptionMetered;
import com.codahale.metrics.annotation.Gauge;
import com.codahale.metrics.annotation.Metered;
import com.codahale.metrics.annotation.Timed;
import com.ryantenney.metrics.annotation.CachedGauge;
import com.ryantenney.metrics.annotation.Counted;
import com.ryantenney.metrics.annotation.Metric;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public interface NamingStrategy {

	String forTimedMethod(Class<?> klass, String beanName, Method method, Timed annotation);

	String forMeteredMethod(Class<?> klass, String beanName, Method method, Metered annotation);

	String forExceptionMeteredMethod(Class<?> klass, String beanName, Method method, ExceptionMetered annotation);

	String forCountedMethod(Class<?> klass, String beanName, Method method, Counted annotation);

	String forGaugeField(Class<?> klass, String beanName, Field field, Gauge annotation);

	String forGaugeMethod(Class<?> klass, String beanName, Method method, Gauge annotation);

	String forCachedGaugeMethod(Class<?> klass, String beanName, Method method, CachedGauge annotation);

	String forMetricField(Class<?> klass, String beanName, Field field, Metric annotation);

}
