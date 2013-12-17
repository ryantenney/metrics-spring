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
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.CachedGauge;
import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.codahale.metrics.annotation.ExceptionMetered;
import com.codahale.metrics.annotation.Metered;
import com.codahale.metrics.annotation.Timed;
import com.ryantenney.metrics.annotation.Counted;
import com.ryantenney.metrics.annotation.InjectMetric;
import com.ryantenney.metrics.annotation.Metric;

@SuppressWarnings("deprecation")
class TestUtil {

	private static final Logger log = LoggerFactory.getLogger(TestUtil.class);

	private static final DefaultNamingStrategy DEFAULT_NAMING_STRATEGY = new DefaultNamingStrategy();

	static String forTimedMethod(Class<?> klass, Method method, Timed annotation) {
		return DEFAULT_NAMING_STRATEGY.forTimedMethod(klass, null, method, annotation);
	}

	static String forMeteredMethod(Class<?> klass, Method method, Metered annotation) {
		return DEFAULT_NAMING_STRATEGY.forMeteredMethod(klass, null, method, annotation);
	}

	static String forGaugeMethod(Class<?> klass, Method method, com.codahale.metrics.annotation.Gauge annotation) {
		return DEFAULT_NAMING_STRATEGY.forGaugeMethod(klass, null, method, annotation);
	}

	static String forGaugeField(Class<?> klass, Field field, com.codahale.metrics.annotation.Gauge annotation) {
		return DEFAULT_NAMING_STRATEGY.forGaugeField(klass, null, field, annotation);
	}

	static String forCachedGauge(Class<?> klass, Method method, com.ryantenney.metrics.annotation.CachedGauge annotation) {
		return DEFAULT_NAMING_STRATEGY.forCachedGaugeMethod(klass, null, method, annotation);
	}

	static String forExceptionMeteredMethod(Class<?> klass, Method method, ExceptionMetered annotation) {
		return DEFAULT_NAMING_STRATEGY.forExceptionMeteredMethod(klass, null, method, annotation);
	}

	static String forCountedMethod(Class<?> klass, Method method, Counted annotation) {
		return DEFAULT_NAMING_STRATEGY.forCountedMethod(klass, null, method, annotation);
	}

	static String forMetricField(Class<?> klass, Field field, Metric annotation) {
		return DEFAULT_NAMING_STRATEGY.forMetricField(klass, null, field, annotation);
	}

	static String forInjectMetricField(Class<?> klass, Field field, InjectMetric annotation) {
		return DEFAULT_NAMING_STRATEGY.chooseName(annotation.name(), annotation.absolute(), klass, null, field);
	}

	static Gauge<?> forGaugeField(MetricRegistry metricRegistry, Class<?> clazz, String fieldName) {
		Field field = findField(clazz, fieldName);
		String metricName = forGaugeField(clazz, field, field.getAnnotation(com.codahale.metrics.annotation.Gauge.class));
		log.info("Looking up gauge field named '{}'", metricName);
		return metricRegistry.getGauges().get(metricName);
	}

	static Gauge<?> forGaugeMethod(MetricRegistry metricRegistry, Class<?> clazz, String methodName) {
		Method method = findMethod(clazz, methodName);
		String metricName = forGaugeMethod(clazz, method, method.getAnnotation(com.codahale.metrics.annotation.Gauge.class));
		log.info("Looking up gauge method named '{}'", metricName);
		return metricRegistry.getGauges().get(metricName);
	}

	static CachedGauge<?> forCachedGaugeMethod(MetricRegistry metricRegistry, Class<?> clazz, String methodName) {
		Method method = findMethod(clazz, methodName);
		String metricName = forCachedGauge(clazz, method, method.getAnnotation(com.ryantenney.metrics.annotation.CachedGauge.class));
		log.info("Looking up cached gauge method named '{}'", metricName);
		return (CachedGauge<?>) metricRegistry.getGauges().get(metricName);
	}

	static Timer forTimedMethod(MetricRegistry metricRegistry, Class<?> clazz, String methodName) {
		Method method = findMethod(clazz, methodName);
		String metricName = forTimedMethod(clazz, method, method.getAnnotation(Timed.class));
		log.info("Looking up timed method named '{}'", metricName);
		return metricRegistry.getTimers().get(metricName);
	}

	static Meter forMeteredMethod(MetricRegistry metricRegistry, Class<?> clazz, String methodName) {
		Method method = findMethod(clazz, methodName);
		String metricName = forMeteredMethod(clazz, method, method.getAnnotation(Metered.class));
		log.info("Looking up metered method named '{}'", metricName);
		return metricRegistry.getMeters().get(metricName);
	}

	static Meter forExceptionMeteredMethod(MetricRegistry metricRegistry, Class<?> clazz, String methodName) {
		Method method = findMethod(clazz, methodName);
		String metricName = forExceptionMeteredMethod(clazz, method, method.getAnnotation(ExceptionMetered.class));
		log.info("Looking up exception metered method named '{}'", metricName);
		return metricRegistry.getMeters().get(metricName);
	}

	static Counter forCountedMethod(MetricRegistry metricRegistry, Class<?> clazz, String methodName) {
		Method method = findMethod(clazz, methodName);
		String metricName = forCountedMethod(clazz, method, method.getAnnotation(Counted.class));
		log.info("Looking up counted method named '{}'", metricName);
		return metricRegistry.getCounters().get(metricName);
	}

	@Deprecated
	static com.codahale.metrics.Metric forInjectMetricField(MetricRegistry metricRegistry, Class<?> clazz, String fieldName) {
		Field field = findField(clazz, fieldName);
		String metricName = forInjectMetricField(clazz, field, field.getAnnotation(InjectMetric.class));
		return getMetric(metricRegistry, field.getType(), metricName);
	}

	static com.codahale.metrics.Metric forMetricField(MetricRegistry metricRegistry, Class<?> clazz, String fieldName) {
		Field field = findField(clazz, fieldName);
		String metricName = forMetricField(clazz, field, field.getAnnotation(Metric.class));
		return getMetric(metricRegistry, field.getType(), metricName);
	}

	private static com.codahale.metrics.Metric getMetric(MetricRegistry metricRegistry, Class<?> type, String metricName) {
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
