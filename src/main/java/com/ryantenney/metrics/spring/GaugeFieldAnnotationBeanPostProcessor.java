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

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.annotation.Gauge;

import static com.ryantenney.metrics.spring.AnnotationFilter.INSTANCE_FIELDS;

class GaugeFieldAnnotationBeanPostProcessor extends AbstractAnnotationBeanPostProcessor implements Ordered {

	private static final AnnotationFilter FILTER = new AnnotationFilter(Gauge.class, INSTANCE_FIELDS);

	private final MetricRegistry metrics;

	public GaugeFieldAnnotationBeanPostProcessor(final MetricRegistry metrics) {
		super(Members.ALL, Phase.PRE_INIT, FILTER);
		this.metrics = metrics;
	}

	@Override
	protected void withField(final Object bean, String beanName, Class<?> targetClass, final Field field) {
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

	@Override
	public int getOrder() {
		return LOWEST_PRECEDENCE;
	}

}
