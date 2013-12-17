package com.ryantenney.metrics.spring;

import com.codahale.metrics.annotation.ExceptionMetered;
import com.codahale.metrics.annotation.Gauge;
import com.codahale.metrics.annotation.Metered;
import com.codahale.metrics.annotation.Timed;
import com.ryantenney.metrics.annotation.CachedGauge;
import com.ryantenney.metrics.annotation.Counted;
import com.ryantenney.metrics.annotation.Metric;

import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;

public abstract class AbstractNamingStrategy implements NamingStrategy {

	@Override
	public String forTimedMethod(Class<?> klass, String beanName, Method method, Timed annotation) {
		return chooseName(annotation.name(), annotation.absolute(), klass, beanName, method);
	}

	@Override
	public String forMeteredMethod(Class<?> klass, String beanName, Method method, Metered annotation) {
		return chooseName(annotation.name(), annotation.absolute(), klass, beanName, method);
	}

	@Override
	public String forExceptionMeteredMethod(Class<?> klass, String beanName, Method method, ExceptionMetered annotation) {
		return chooseName(annotation.name(), annotation.absolute(), klass, beanName, method, ExceptionMetered.DEFAULT_NAME_SUFFIX);
	}

	@Override
	public String forCountedMethod(Class<?> klass, String beanName, Method method, Counted annotation) {
		return chooseName(annotation.name(), annotation.absolute(), klass, beanName, method);
	}

	@Override
	public String forGaugeMethod(Class<?> klass, String beanName, Method method, Gauge annotation) {
		return chooseName(annotation.name(), annotation.absolute(), klass, beanName, method);
	}

	@Override
	public String forGaugeField(Class<?> klass, String beanName, Field field, Gauge annotation) {
		return chooseName(annotation.name(), annotation.absolute(), klass, beanName, field);
	}

	@Override
	public String forCachedGaugeMethod(Class<?> klass, String beanName, Method method, CachedGauge annotation) {
		return chooseName(annotation.name(), annotation.absolute(), klass, beanName, method);
	}

	@Override
	public String forMetricField(Class<?> klass, String beanName, Field field, Metric annotation) {
		return chooseName(annotation.name(), annotation.absolute(), klass, beanName, field);
	}

	protected abstract String chooseName(String explicitName, boolean absolute, Class<?> klass, String beanName, Member member, String... suffixes);

}
