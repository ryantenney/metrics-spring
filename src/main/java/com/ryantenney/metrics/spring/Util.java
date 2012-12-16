package com.ryantenney.metrics.spring;

import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Field;

import com.yammer.metrics.annotation.*;
import com.yammer.metrics.core.MetricName;

import static com.yammer.metrics.core.MetricName.*;

class Util {

	private static String chooseName(String name, Member field) {
		if (name == null || name.isEmpty()) {
			name = field.getName();
		}
		return name;
	}

	public static MetricName forTimedMethod(Class<?> klass, Method method, Timed annotation, String scope) {
		return new MetricName(chooseGroup(annotation.group(), klass),
							  chooseType(annotation.type(), klass),
							  chooseName(annotation.name(), method),
							  scope);
	}

	public static MetricName forMeteredMethod(Class<?> klass, Method method, Metered annotation, String scope) {
		return new MetricName(chooseGroup(annotation.group(), klass),
							  chooseType(annotation.type(), klass),
							  chooseName(annotation.name(), method),
							  scope);
	}

	public static MetricName forGauge(Class<?> klass, Member member, Gauge annotation, String scope) {
		return new MetricName(chooseGroup(annotation.group(), klass),
							  chooseType(annotation.type(), klass),
							  chooseName(annotation.name(), member),
							  scope);
	}

	public static MetricName forExceptionMeteredMethod(Class<?> klass, Method method, ExceptionMetered annotation, String scope) {
		return new MetricName(chooseGroup(annotation.group(), klass),
							  chooseType(annotation.type(), klass),
							  annotation.name().isEmpty()
								  ? method.getName() + ExceptionMetered.DEFAULT_NAME_SUFFIX
								  : annotation.name(),
							  scope);
	}

}
