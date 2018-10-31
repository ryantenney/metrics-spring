/**
 * Copyright © 2012 Ryan W Tenney (ryan@10e.us)
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

import org.springframework.core.Ordered;
import org.springframework.util.ReflectionUtils;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.ryantenney.metrics.annotation.Metric;

import static com.ryantenney.metrics.spring.AnnotationFilter.INJECTABLE_FIELDS;

@Deprecated
class LegacyMetricAnnotationBeanPostProcessor extends AbstractAnnotationBeanPostProcessor implements Ordered {

	private static final AnnotationFilter FILTER = new AnnotationFilter(Metric.class, INJECTABLE_FIELDS);

	private final MetricRegistry metrics;

	public LegacyMetricAnnotationBeanPostProcessor(final MetricRegistry metrics) {
		super(Members.FIELDS, Phase.PRE_INIT, FILTER);
		this.metrics = metrics;
	}

	@Override
	protected void withField(Object bean, String beanName, Class<?> targetClass, Field field) {
		final Metric annotation = field.getAnnotation(Metric.class);
		final String metricName = Util.forMetricField(targetClass, field, annotation);

		final Class<?> type = field.getType();
		if (!com.codahale.metrics.Metric.class.isAssignableFrom(type)) {
			throw new IllegalArgumentException("Field " + targetClass.getCanonicalName() + "." + field.getName() + " must be a subtype of "
					+ com.codahale.metrics.Metric.class.getCanonicalName());
		}

		ReflectionUtils.makeAccessible(field);

		// Get the value of the field annotated with @Metric
		com.codahale.metrics.Metric metric = (com.codahale.metrics.Metric) ReflectionUtils.getField(field, bean);

		if (metric == null) {
			// If null, create a metric of the appropriate type and inject it
			metric = getMetric(metrics, type, metricName);
			ReflectionUtils.setField(field, bean, metric);
			LOG.debug("Injected metric {} for field {}.{}", metricName, targetClass.getCanonicalName(), field.getName());
		}
		else {
			// If non-null, register that instance of the metric
			try {
				// Attempt to register that instance of the metric
				metrics.register(metricName, metric);
				LOG.debug("Registered metric {} for field {}.{}", metricName, targetClass.getCanonicalName(), field.getName());
			}
			catch (IllegalArgumentException ex1) {
				// A metric is already registered under that name
				// (Cannot determine the cause without parsing the Exception's message)
				try {
					metric = getMetric(metrics, type, metricName);
					ReflectionUtils.setField(field, bean, metric);
					LOG.debug("Injected metric {} for field {}.{}", metricName, targetClass.getCanonicalName(), field.getName());
				}
				catch (IllegalArgumentException ex2) {
					// A metric of a different type is already registered under that name
					throw new IllegalArgumentException("Error injecting metric for field " + targetClass.getCanonicalName() + "." + field.getName(), ex2);
				}
			}
		}
	}

	private com.codahale.metrics.Metric getMetric(MetricRegistry metricRegistry, Class<?> type, String metricName) {
		com.codahale.metrics.Metric metric;
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
			throw new IllegalArgumentException("Invalid @Metric type " + type.getCanonicalName());
		}
		return metric;
	}

	@Override
	public int getOrder() {
		return LOWEST_PRECEDENCE - 2;
	}

}
