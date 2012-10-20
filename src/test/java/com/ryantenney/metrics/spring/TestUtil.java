/*
 * Copyright 2012 Ryan W Tenney (http://ryan.10e.us)
 *            and Martello Technologies (http://martellotech.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
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

import org.springframework.util.ReflectionUtils;

import com.yammer.metrics.annotation.ExceptionMetered;
import com.yammer.metrics.annotation.Metered;
import com.yammer.metrics.annotation.Timed;
import com.yammer.metrics.core.Gauge;
import com.yammer.metrics.core.Meter;
import com.yammer.metrics.core.MetricName;
import com.yammer.metrics.core.MetricsRegistry;
import com.yammer.metrics.core.Timer;

/**
 * This exists in part to get access to the Util class in testing.
 */
class TestUtil extends Util {

	public static Gauge<?> forGaugeField(MetricsRegistry metricsRegistry, Class<?> clazz, String fieldName) {
		Field field = ReflectionUtils.findField(clazz, fieldName);
		MetricName metricName = forGauge(clazz, field, field.getAnnotation(com.yammer.metrics.annotation.Gauge.class), null);
		return (Gauge<?>) metricsRegistry.getAllMetrics().get(metricName);
	}

	public static Gauge<?> forGaugeMethod(MetricsRegistry metricsRegistry, Class<?> clazz, String methodName) {
		Method method = getMethodByName(clazz, methodName);
		MetricName metricName = forGauge(clazz, method, method.getAnnotation(com.yammer.metrics.annotation.Gauge.class), null);
		return (Gauge<?>) metricsRegistry.getAllMetrics().get(metricName);
	}

	public static Timer forTimedMethod(MetricsRegistry metricsRegistry, Class<?> clazz, String methodName) {
		Method method = getMethodByName(clazz, methodName);
		MetricName metricName = forTimedMethod(clazz, method, method.getAnnotation(Timed.class), null);
		return (Timer) metricsRegistry.getAllMetrics().get(metricName);
	}

	public static Meter forMeteredMethod(MetricsRegistry metricsRegistry, Class<?> clazz, String methodName) {
		Method method = getMethodByName(clazz, methodName);
		MetricName metricName = forMeteredMethod(clazz, method, method.getAnnotation(Metered.class), null);
		return (Meter) metricsRegistry.getAllMetrics().get(metricName);
	}

	public static Meter forExceptionMeteredMethod(MetricsRegistry metricsRegistry, Class<?> clazz, String methodName) {
		Method method = getMethodByName(clazz, methodName);
		MetricName metricName = forExceptionMeteredMethod(clazz, method, method.getAnnotation(ExceptionMetered.class), null);
		return (Meter) metricsRegistry.getAllMetrics().get(metricName);
	}

	private static Method getMethodByName(Class<?> clazz, String methodName) {
		List<Method> methodsFound = new ArrayList<Method>();
		for (Method method : clazz.getMethods()) {
			if (method.getName().equals(methodName)) {
				methodsFound.add(method);
			}
		}
		if (methodsFound.size() == 1) {
			return methodsFound.get(0);
		} else {
			throw new RuntimeException("No unique method " + methodName + " found on class " + clazz.getName());
		}
	}

}