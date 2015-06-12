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

import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.dropwizard.metrics.annotation.ExceptionMetered;
import io.dropwizard.metrics.annotation.Metered;
import io.dropwizard.metrics.annotation.Timed;

import io.dropwizard.metrics.CachedGauge;
import io.dropwizard.metrics.Counter;
import io.dropwizard.metrics.Gauge;
import io.dropwizard.metrics.Histogram;
import io.dropwizard.metrics.Meter;
import io.dropwizard.metrics.MetricName;
import io.dropwizard.metrics.MetricRegistry;
import io.dropwizard.metrics.Timer;

import com.ryantenney.metrics.annotation.Counted;
import com.ryantenney.metrics.annotation.Metric;

class TestUtil {

	private static final Logger log = LoggerFactory.getLogger(TestUtil.class);

	static MetricName forTimedMethod(Class<?> klass, Member member, Timed annotation) {
		return Util.forTimedMethod(klass, member, annotation);
	}

	static MetricName forMeteredMethod(Class<?> klass, Member member, Metered annotation) {
		return Util.forMeteredMethod(klass, member, annotation);
	}

	static MetricName forGauge(Class<?> klass, Member member, io.dropwizard.metrics.annotation.Gauge annotation) {
		return Util.forGauge(klass, member, annotation);
	}

	static MetricName forCachedGauge(Class<?> klass, Member member, com.ryantenney.metrics.annotation.CachedGauge annotation) {
		return Util.forCachedGauge(klass, member, annotation);
	}

	static MetricName forExceptionMeteredMethod(Class<?> klass, Member member, ExceptionMetered annotation) {
		return Util.forExceptionMeteredMethod(klass, member, annotation);
	}

	static MetricName forCountedMethod(Class<?> klass, Member member, Counted annotation) {
		return Util.forCountedMethod(klass, member, annotation);
	}

	static MetricName forMetricField(Class<?> klass, Member member, Metric annotation) {
		return Util.forMetricField(klass, member, annotation);
	}

	static Gauge<?> forGaugeField(MetricRegistry metricRegistry, Class<?> clazz, String fieldName) {
		Field field = findField(clazz, fieldName);
		MetricName metricName = forGauge(clazz, field, field.getAnnotation(io.dropwizard.metrics.annotation.Gauge.class));
		log.info("Looking up gauge field named '{}'", metricName);
		return metricRegistry.getGauges().get(metricName);
	}

	static Gauge<?> forGaugeMethod(MetricRegistry metricRegistry, Class<?> clazz, String methodName) {
		Method method = findMethod(clazz, methodName);
		MetricName metricName = forGauge(clazz, method, method.getAnnotation(io.dropwizard.metrics.annotation.Gauge.class));
		log.info("Looking up gauge method named '{}'", metricName);
		return metricRegistry.getGauges().get(metricName);
	}

	static CachedGauge<?> forCachedGaugeMethod(MetricRegistry metricRegistry, Class<?> clazz, String methodName) {
		Method method = findMethod(clazz, methodName);
		MetricName metricName = forCachedGauge(clazz, method, method.getAnnotation(com.ryantenney.metrics.annotation.CachedGauge.class));
		log.info("Looking up cached gauge method named '{}'", metricName);
		return (CachedGauge<?>) metricRegistry.getGauges().get(metricName);
	}

	static Timer forTimedMethod(MetricRegistry metricRegistry, Class<?> clazz, String methodName) {
		Method method = findMethod(clazz, methodName);
		MetricName metricName = forTimedMethod(clazz, method, method.getAnnotation(Timed.class));
		log.info("Looking up timed method named '{}'", metricName);
		return metricRegistry.getTimers().get(metricName);
	}

	static Meter forMeteredMethod(MetricRegistry metricRegistry, Class<?> clazz, String methodName) {
		Method method = findMethod(clazz, methodName);
		MetricName metricName = forMeteredMethod(clazz, method, method.getAnnotation(Metered.class));
		log.info("Looking up metered method named '{}'", metricName);
		return metricRegistry.getMeters().get(metricName);
	}

	static Meter forExceptionMeteredMethod(MetricRegistry metricRegistry, Class<?> clazz, String methodName) {
		Method method = findMethod(clazz, methodName);
		MetricName metricName = forExceptionMeteredMethod(clazz, method, method.getAnnotation(ExceptionMetered.class));
		log.info("Looking up exception metered method named '{}'", metricName);
		return metricRegistry.getMeters().get(metricName);
	}

	static Counter forCountedMethod(MetricRegistry metricRegistry, Class<?> clazz, String methodName) {
		Method method = findMethod(clazz, methodName);
		MetricName metricName = forCountedMethod(clazz, method, method.getAnnotation(Counted.class));
		log.info("Looking up counted method named '{}'", metricName);
		return metricRegistry.getCounters().get(metricName);
	}

	static io.dropwizard.metrics.Metric forMetricField(MetricRegistry metricRegistry, Class<?> clazz, String fieldName) {
		Field field = findField(clazz, fieldName);
		MetricName metricName = forMetricField(clazz, field, field.getAnnotation(Metric.class));
		return getMetric(metricRegistry, field.getType(), metricName);
	}

	private static io.dropwizard.metrics.Metric getMetric(MetricRegistry metricRegistry, Class<?> type, MetricName metricName) {
		log.info("Looking up injected metric field named '{}'", metricName);
		if (type.isAssignableFrom(Meter.class)) {
			return metricRegistry.getMeters().get(metricName);
		}
		else if (type.isAssignableFrom(Timer.class)) {
			return metricRegistry.getTimers().get(metricName);
		}
		else if (type.isAssignableFrom(Counter.class)) {
			return metricRegistry.getCounters().get(metricName);
		}
		else if (type.isAssignableFrom(Histogram.class)) {
			return metricRegistry.getHistograms().get(metricName);
		}
		return null;
	}

	private static Field findField(Class<?> clazz, String fieldName) {
		List<Field> fieldsFound = new ArrayList<Field>();
		for (Field field : clazz.getDeclaredFields()) {
			if (field.getName().equals(fieldName)) {
				fieldsFound.add(field);
			}
		}
		if (fieldsFound.size() == 1) {
			return fieldsFound.get(0);
		}
		else {
			throw new RuntimeException("No unique field " + fieldName + " found on class " + clazz.getName());
		}
	}

	private static Method findMethod(Class<?> clazz, String methodName) {
		log.info("Looking for method {}.{}", clazz, methodName);
		List<Method> methodsFound = new ArrayList<Method>();
		for (Method method : clazz.getDeclaredMethods()) {
			if (method.getName().equals(methodName)) {
				methodsFound.add(method);
			}
		}
		if (methodsFound.size() == 1) {
			return methodsFound.get(0);
		}
		else {
			throw new RuntimeException("No unique method " + methodName + " found on class " + clazz.getName());
		}
	}

}
