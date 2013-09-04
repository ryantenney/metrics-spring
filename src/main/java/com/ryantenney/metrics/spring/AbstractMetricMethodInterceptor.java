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

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.ReflectionUtils.MethodCallback;
import org.springframework.util.ReflectionUtils.MethodFilter;

import com.codahale.metrics.MetricRegistry;

abstract class AbstractMetricMethodInterceptor<A extends Annotation, M> implements MethodInterceptor, MethodCallback {

	protected final Logger LOG = LoggerFactory.getLogger(getClass());

	private final MetricRegistry metricRegistry;
	private final Class<?> targetClass;
	private final Class<A> annotationClass;
	private final Map<MethodKey, M> metrics;

	AbstractMetricMethodInterceptor(final MetricRegistry metricRegistry, final Class<?> targetClass, final Class<A> annotationClass, final MethodFilter methodFilter) {
		this.metricRegistry = metricRegistry;
		this.targetClass = targetClass;
		this.annotationClass = annotationClass;
		this.metrics = new HashMap<MethodKey, M>();

		LOG.debug("Creating method interceptor for class {}", targetClass.getCanonicalName());
		LOG.debug("Scanning for @{} annotated methods", annotationClass.getSimpleName());

		ReflectionUtils.doWithMethods(targetClass, this, methodFilter);
	}

	@Override
	public Object invoke(MethodInvocation invocation) throws Throwable {
		final M metric = metrics.get(MethodKey.forMethod(invocation.getMethod()));
		if (metric != null) {
			return invoke(invocation, metric);
		}
		else {
			return invocation.proceed();
		}
	}

	@Override
	public void doWith(Method method) throws IllegalAccessException {
		final A annotation = method.getAnnotation(annotationClass);
		final MethodKey methodKey = MethodKey.forMethod(method);
		final String metricName = buildMetricName(targetClass, method, annotation);
		final M metric = buildMetric(metricRegistry, metricName, annotation);

		metrics.put(methodKey, metric);

		if (LOG.isDebugEnabled()) {
			LOG.debug("Created {} {} for method {}", metric.getClass().getSimpleName(), metricName, methodKey);
		}
	}

	protected abstract String buildMetricName(Class<?> targetClass, Method method, A annotation);

	protected abstract M buildMetric(MetricRegistry metricRegistry, String metricName, A annotation);

	protected abstract Object invoke(MethodInvocation invocation, M metric) throws Throwable;

}
