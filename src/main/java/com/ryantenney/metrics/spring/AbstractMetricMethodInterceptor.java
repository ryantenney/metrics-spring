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
	private final Map<MethodKey, AnnotationMetricPair<A, M>> metrics;

	AbstractMetricMethodInterceptor(final MetricRegistry metricRegistry, final Class<?> targetClass, final Class<A> annotationClass, final MethodFilter methodFilter) {
		this.metricRegistry = metricRegistry;
		this.targetClass = targetClass;
		this.annotationClass = annotationClass;
		this.metrics = new HashMap<MethodKey, AnnotationMetricPair<A, M>>();

		LOG.debug("Creating method interceptor for class {}", targetClass.getCanonicalName());
		LOG.debug("Scanning for @{} annotated methods", annotationClass.getSimpleName());

		ReflectionUtils.doWithMethods(targetClass, this, methodFilter);
	}

	@Override
	public Object invoke(MethodInvocation invocation) throws Throwable {
		final AnnotationMetricPair<A, M> annotationMetricPair = metrics.get(MethodKey.forMethod(invocation.getMethod()));
		if (annotationMetricPair != null) {
			return invoke(invocation, annotationMetricPair.getMeter(), annotationMetricPair.getAnnotation());
		}
		else {
			return invocation.proceed();
		}
	}

	@Override
	public void doWith(Method method) throws IllegalAccessException {
		final A annotation = method.getAnnotation(annotationClass);
		if (annotation != null) {
			final MethodKey methodKey = MethodKey.forMethod(method);
			final String metricName = buildMetricName(targetClass, method, annotation);
			final M metric = buildMetric(metricRegistry, metricName, annotation);

			if (metric != null) {
				metrics.put(methodKey, new AnnotationMetricPair<A, M>(annotation, metric));

				if (LOG.isDebugEnabled()) {
					LOG.debug("Created {} {} for method {}", metric.getClass().getSimpleName(), metricName, methodKey);
				}
			}
		}
	}

	protected abstract String buildMetricName(Class<?> targetClass, Method method, A annotation);

	protected abstract M buildMetric(MetricRegistry metricRegistry, String metricName, A annotation);

	protected abstract Object invoke(MethodInvocation invocation, M metric, A annotation) throws Throwable;

	public static final class AnnotationMetricPair<A extends Annotation, M> {

		private final A annotation;
		private final M meter;

		public AnnotationMetricPair(final A annotation, final M meter) {
			this.annotation = annotation;
			this.meter = meter;
		}

		public A getAnnotation() {
			return annotation;
		}

		public M getMeter() {
			return meter;
		}

	}

}
