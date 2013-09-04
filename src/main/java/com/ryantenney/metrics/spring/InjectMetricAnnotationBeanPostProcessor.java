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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.ReflectionUtils.FieldCallback;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.ryantenney.metrics.annotation.InjectMetric;

class InjectMetricAnnotationBeanPostProcessor implements BeanPostProcessor, Ordered {

	private static final Logger LOG = LoggerFactory.getLogger(InjectMetricAnnotationBeanPostProcessor.class);

	private static final AnnotationFilter FILTER = new AnnotationFilter(InjectMetric.class);

	private final MetricRegistry metrics;

	public InjectMetricAnnotationBeanPostProcessor(final MetricRegistry metrics) {
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
			public void doWith(Field field) throws IllegalAccessException {
				final InjectMetric annotation = field.getAnnotation(InjectMetric.class);
				final String metricName = Util.forInjectMetricField(targetClass, field, annotation);

				final Class<?> type = field.getType();
				Metric metric = null;
				if (Meter.class == type) {
					metric = metrics.meter(metricName);
				}
				else if (Timer.class == type) {
					metric = metrics.timer(metricName);
				}
				else if (Counter.class == type) {
					metric = metrics.counter(metricName);
				}
				else if (Histogram.class == type) {
					metric = metrics.histogram(metricName);
				}
				else {
					throw new IllegalStateException("Cannot inject a metric of type " + type.getCanonicalName());
				}

				ReflectionUtils.makeAccessible(field);
				ReflectionUtils.setField(field, bean, metric);

				LOG.debug("Injected metric {} for field {}.{}", metricName, targetClass.getCanonicalName(), field.getName());
			}
		}, FILTER);

		return bean;
	}

	@Override
	public int getOrder() {
		return LOWEST_PRECEDENCE - 2;
	}

}
