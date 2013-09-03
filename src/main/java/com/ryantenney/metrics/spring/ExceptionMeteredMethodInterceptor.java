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

import java.lang.reflect.Method;

import org.aopalliance.aop.Advice;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.Pointcut;
import org.springframework.aop.support.annotation.AnnotationMatchingPointcut;
import org.springframework.core.Ordered;
import org.springframework.util.ReflectionUtils.MethodFilter;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.annotation.ExceptionMetered;
import com.ryantenney.metrics.spring.ExceptionMeteredMethodInterceptor.ExceptionMeter;

class ExceptionMeteredMethodInterceptor extends AbstractMetricMethodInterceptor<ExceptionMetered, ExceptionMeter> implements Ordered {

	public static final Class<ExceptionMetered> ANNOTATION = ExceptionMetered.class;
	public static final Pointcut POINTCUT = new AnnotationMatchingPointcut(null, ANNOTATION);
	public static final MethodFilter METHOD_FILTER = new AnnotationFilter(ANNOTATION);

	public ExceptionMeteredMethodInterceptor(final MetricRegistry metricRegistry, final Class<?> targetClass) {
		super(metricRegistry, targetClass, ANNOTATION, METHOD_FILTER);
	}

	@Override
	protected Object invoke(MethodInvocation invocation, ExceptionMeter metric) throws Throwable {
		try {
			return invocation.proceed();
		}
		catch (Throwable t) {
			if (metric != null && metric.getCause().isAssignableFrom(t.getClass())) {
				metric.getMeter().mark();
			}
			throw t;
		}
	}

	@Override
	protected ExceptionMeter buildMetric(MetricRegistry metricRegistry, String metricName, ExceptionMetered annotation) {
		final Meter meter = metricRegistry.meter(metricName);
		return new ExceptionMeter(meter, annotation.cause());
	}
	
	@Override
	protected String buildMetricName(Class<?> targetClass, Method method, ExceptionMetered annotation) {
		return Util.forExceptionMeteredMethod(targetClass, method, annotation);
	}

	@Override
	public int getOrder() {
		return HIGHEST_PRECEDENCE;
	}

	static class ExceptionMeter {

		private final Meter meter;
		private final Class<? extends Throwable> cause;

		public ExceptionMeter(final Meter meter, final Class<? extends Throwable> cause) {
			this.meter = meter;
			this.cause = cause;
		}

		public Meter getMeter() {
			return meter;
		}

		public Class<? extends Throwable> getCause() {
			return cause;
		}

	}

	static AdviceFactory adviceFactory(final MetricRegistry metricRegistry) {
		return new AdviceFactory() {
			@Override
			public Advice getAdvice(Object bean, Class<?> targetClass) {
				return new ExceptionMeteredMethodInterceptor(metricRegistry, targetClass);
			}
		};
	}

}
