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
import java.util.HashMap;
import java.util.Map;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.Pointcut;
import org.springframework.aop.support.annotation.AnnotationMatchingPointcut;
import org.springframework.core.Ordered;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.ReflectionUtils.MethodCallback;
import org.springframework.util.ReflectionUtils.MethodFilter;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.annotation.ExceptionMetered;

class ExceptionMeteredMethodInterceptor implements MethodInterceptor, MethodCallback, Ordered {

	private static final Logger LOG = LoggerFactory.getLogger(ExceptionMeteredMethodInterceptor.class);

	public static final Pointcut POINTCUT = new AnnotationMatchingPointcut(null, ExceptionMetered.class);
	public static final MethodFilter METHOD_FILTER = new AnnotationFilter(ExceptionMetered.class);

	private final MetricRegistry metrics;
	private final Class<?> targetClass;
	private final Map<MethodKey, Meter> meters;
	private final Map<MethodKey, Class<? extends Throwable>> causes;

	public ExceptionMeteredMethodInterceptor(final MetricRegistry metrics, final Class<?> targetClass) {
		this.metrics = metrics;
		this.targetClass = targetClass;
		this.meters = new HashMap<MethodKey, Meter>();
		this.causes = new HashMap<MethodKey, Class<? extends Throwable>>();

		LOG.debug("Creating method interceptor for class {}", targetClass.getCanonicalName());
		LOG.debug("Scanning for @ExceptionMetered annotated methods");

		ReflectionUtils.doWithMethods(targetClass, this, METHOD_FILTER);
	}

	@Override
	public Object invoke(MethodInvocation invocation) throws Throwable {
		try {
			return invocation.proceed();
		}
		catch (Throwable t) {
			final MethodKey key = MethodKey.forMethod(invocation.getMethod());
			final Class<?> cause = causes.get(key);
			if (cause != null && cause.isAssignableFrom(t.getClass())) {
				// it may be safe to infer that `meter` is non-null if `cause` is non-null
				final Meter meter = meters.get(key);
				if (meter != null) {
					meter.mark();
				}
			}
			throw t;
		}
	}

	@Override
	public void doWith(Method method) throws IllegalAccessException {
		final ExceptionMetered annotation = method.getAnnotation(ExceptionMetered.class);
		final String metricName = Util.forExceptionMeteredMethod(targetClass, method, annotation);
		final MethodKey methodKey = MethodKey.forMethod(method);
		final Meter meter = metrics.meter(metricName);

		meters.put(methodKey, meter);
		causes.put(methodKey, annotation.cause());

		LOG.debug("Created Meter {} for method {}", metricName, methodKey);
	}

	@Override
	public int getOrder() {
		return HIGHEST_PRECEDENCE;
	}

}
