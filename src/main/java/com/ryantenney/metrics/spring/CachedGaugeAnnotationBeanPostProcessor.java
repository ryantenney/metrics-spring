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

import java.lang.reflect.Method;

import org.springframework.core.Ordered;
import org.springframework.util.ReflectionUtils;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.annotation.CachedGauge;

import static com.ryantenney.metrics.spring.AnnotationFilter.INSTANCE_METHODS;

class CachedGaugeAnnotationBeanPostProcessor extends AbstractAnnotationBeanPostProcessor implements Ordered {

	private static final AnnotationFilter FILTER = new AnnotationFilter(CachedGauge.class, INSTANCE_METHODS);

	private final MetricRegistry metrics;

	public CachedGaugeAnnotationBeanPostProcessor(final MetricRegistry metrics) {
		super(Members.METHODS, Phase.POST_INIT, FILTER);
		this.metrics = metrics;
	}

	@Override
	protected void withMethod(final Object bean, String beanName, Class<?> targetClass, final Method method) {
		if (method.getParameterTypes().length > 0) {
			throw new IllegalStateException("Method " + method.getName() + " is annotated with @CachedGauge but requires parameters.");
		}

		final CachedGauge annotation = method.getAnnotation(CachedGauge.class);
		final String metricName = Util.forCachedGauge(targetClass, method, annotation);

		metrics.register(metricName, new com.codahale.metrics.CachedGauge<Object>(annotation.timeout(), annotation.timeoutUnit()) {
			@Override
			protected Object loadValue() {
				return ReflectionUtils.invokeMethod(method, bean);
			}
		});

		LOG.debug("Created cached gauge {} for method {}.{}", metricName, targetClass.getCanonicalName(), method.getName());
	}

	@Override
	public int getOrder() {
		return LOWEST_PRECEDENCE;
	}

}
