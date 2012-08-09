package com.ryantenney.metrics.spring;

import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Field;

import com.yammer.metrics.annotation.*;
import com.yammer.metrics.core.MetricName;

class Util {

	private static String getPackageName(Class<?> klass) {
		return klass.getPackage() == null ? "" : klass.getPackage().getName();
	}

	private static String getClassName(Class<?> klass) {
		return klass.getSimpleName().replaceAll("\\$$", "");
	}

	private static String chooseDomain(String domain, Class<?> klass) {
		if(domain == null || domain.isEmpty()) {
			domain = getPackageName(klass);
		}
		return domain;
	}

	private static String chooseType(String type, Class<?> klass) {
		if(type == null || type.isEmpty()) {
			type = getClassName(klass);
		}
		return type;
	}

	private static String chooseName(String name, Member field) {
		if(name == null || name.isEmpty()) {
			name = field.getName();
		}
		return name;
	}

	public static MetricName forTimedMethod(Class<?> klass, Method method, Timed annotation, String scope) {
		return new MetricName(chooseDomain(annotation.group(), klass),
							  chooseType(annotation.type(), klass),
							  chooseName(annotation.name(), method),
							  scope);
	}

	public static MetricName forMeteredMethod(Class<?> klass, Method method, Metered annotation, String scope) {
		return new MetricName(chooseDomain(annotation.group(), klass),
							  chooseType(annotation.type(), klass),
							  chooseName(annotation.name(), method),
							  scope);
	}

	public static MetricName forGaugeMethod(Class<?> klass, Method method, Gauge annotation, String scope) {
		return new MetricName(chooseDomain(annotation.group(), klass),
							  chooseType(annotation.type(), klass),
							  chooseName(annotation.name(), method),
							  scope);
	}

	public static MetricName forGaugeField(Class<?> klass, Field field, Gauge annotation, String scope) {
		return new MetricName(chooseDomain(annotation.group(), klass),
							  chooseType(annotation.type(), klass),
							  chooseName(annotation.name(), field),
							  scope);
	}

	public static MetricName forExceptionMeteredMethod(Class<?> klass, Method method, ExceptionMetered annotation, String scope) {
		return new MetricName(chooseDomain(annotation.group(), klass),
							  chooseType(annotation.type(), klass),
							  annotation.name().isEmpty()
								  ? method.getName() + ExceptionMetered.DEFAULT_NAME_SUFFIX
								  : annotation.name(),
							  scope);
	}

	public static MetricName forInjectedMetricField(Class<?> klass, Field field, InjectedMetric annotation, String scope) {
		return new MetricName(chooseDomain(annotation.group(), klass),
							  chooseType(annotation.type(), klass),
							  chooseName(annotation.name(), field),
							  scope);
	}

}
