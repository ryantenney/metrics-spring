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

import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.ReflectionUtils.MethodCallback;

import com.codahale.metrics.MetricRegistry;
import com.ryantenney.metrics.annotation.CachedGauge;

class CachedGaugeAnnotationBeanPostProcessor implements BeanPostProcessor, Ordered {

	private static final Logger LOG = LoggerFactory.getLogger(CachedGaugeAnnotationBeanPostProcessor.class);

	private static final AnnotationFilter FILTER = new AnnotationFilter(CachedGauge.class);

	private final MetricRegistry metrics;
    private final NamingStrategy namingStrategy;

    CachedGaugeAnnotationBeanPostProcessor(MetricRegistry metrics, NamingStrategy namingStrategy) {
        this.metrics = metrics;
        this.namingStrategy = namingStrategy;
    }

	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName) {
		return bean;
	}

	@Override
	public Object postProcessAfterInitialization(final Object bean, final String beanName) {
		final Class<?> targetClass = AopUtils.getTargetClass(bean);

		ReflectionUtils.doWithMethods(targetClass, new MethodCallback() {
			@Override
			public void doWith(final Method method) throws IllegalAccessException {
				if (method.getParameterTypes().length > 0) {
					throw new IllegalStateException("Method " + method.getName() +
						" is annotated with @CachedGauge but requires parameters.");
				}

				final CachedGauge annotation = method.getAnnotation(CachedGauge.class);
				final String metricName = namingStrategy.forCachedGauge(targetClass, beanName, method, annotation);

				metrics.register(metricName, new com.codahale.metrics.CachedGauge<Object>(annotation.timeout(), annotation.timeoutUnit()) {
					@Override
					protected Object loadValue() {
						return ReflectionUtils.invokeMethod(method, bean);
					}
				});

				LOG.debug("Created cached gauge {} for method {}.{}", metricName, targetClass.getCanonicalName(), method.getName());
			}
		}, FILTER);

		return bean;
	}

	@Override
	public int getOrder() {
		return LOWEST_PRECEDENCE;
	}

}
