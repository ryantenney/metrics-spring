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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.ReflectionUtils.FieldCallback;
import org.springframework.util.ReflectionUtils.MethodCallback;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.annotation.Gauge;

class GaugeAnnotationBeanPostProcessor implements BeanPostProcessor, Ordered {

	private static final Logger LOG = LoggerFactory.getLogger(GaugeAnnotationBeanPostProcessor.class);

	private static final AnnotationFilter FILTER = new AnnotationFilter(Gauge.class);

	private final MetricRegistry metrics;

	public GaugeAnnotationBeanPostProcessor(final MetricRegistry metrics) {
		this.metrics = metrics;
	}

	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName) {
		return bean;
	}

	@Override
	public Object postProcessAfterInitialization(final Object bean, String beanName) {
		final Class<?> targetClass = AopUtils.getTargetClass(bean);

		ReflectionUtils.doWithFields(targetClass, new FieldCallback() {
			@Override
			public void doWith(final Field field) throws IllegalAccessException {
				ReflectionUtils.makeAccessible(field);

				final Gauge annotation = field.getAnnotation(Gauge.class);
				final String metricName = Util.forGauge(targetClass, field, annotation);

				metrics.register(metricName, new com.codahale.metrics.Gauge<Object>() {
					@Override
					public Object getValue() {
						Object value = ReflectionUtils.getField(field, bean);
						if (value instanceof com.codahale.metrics.Gauge) {
							value = ((com.codahale.metrics.Gauge<?>) value).getValue();
						}
						return value;
					}
				});

				LOG.debug("Created gauge {} for field {}.{}", metricName, targetClass.getCanonicalName(), field.getName());
			}
		}, FILTER);

		ReflectionUtils.doWithMethods(targetClass, new MethodCallback() {
			@Override
			public void doWith(final Method method) throws IllegalAccessException {
				if (method.getParameterTypes().length > 0) {
					throw new IllegalStateException("Method " + method.getName() +
						" is annotated with @Gauge but requires parameters.");
				}

				final Gauge annotation = method.getAnnotation(Gauge.class);
				final String metricName = Util.forGauge(targetClass, method, annotation);

				metrics.register(metricName, new com.codahale.metrics.Gauge<Object>() {
					@Override
					public Object getValue() {
						return ReflectionUtils.invokeMethod(method, bean);
					}
				});

				LOG.debug("Created gauge {} for method {}.{}", metricName, targetClass.getCanonicalName(), method.getName());
			}
		}, FILTER);

		return bean;
	}

	@Override
	public int getOrder() {
		return LOWEST_PRECEDENCE;
	}

}
