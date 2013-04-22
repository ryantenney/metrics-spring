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

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.annotation.ExceptionMetered;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.ReflectionUtils.MethodCallback;
import org.springframework.util.ReflectionUtils.MethodFilter;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

class ExceptionMeteredMethodInterceptor implements MethodInterceptor, MethodCallback, Ordered {

	private static final Logger log = LoggerFactory.getLogger(ExceptionMeteredMethodInterceptor.class);

	private static final MethodFilter filter = new AnnotationFilter(ExceptionMetered.class);

	private final MetricRegistry metrics;
	private final Class<?> targetClass;
	private final Map<Method, Meter> meters;
	private final Map<Method, Class<? extends Throwable>> causes;

	public ExceptionMeteredMethodInterceptor(final MetricRegistry metrics, final Class<?> targetClass) {
		this.metrics = metrics;
		this.targetClass = targetClass;
		this.meters = new HashMap<Method, Meter>();
		this.causes = new HashMap<Method, Class<? extends Throwable>>();

		log.debug("Creating method interceptor for class {}", targetClass.getCanonicalName());
		log.debug("Scanning for @ExceptionMetered annotated methods");

		ReflectionUtils.doWithMethods(targetClass, this, filter);
	}

	@Override
	public Object invoke(MethodInvocation invocation) throws Throwable {
		try {
			return invocation.proceed();
		} catch (Throwable t) {
			final Class<?> cause = causes.get(invocation.getMethod());
			if (cause != null && cause.isAssignableFrom(t.getClass())) {
				// it may be safe to infer that `meter` is non-null if `cause` is non-null
				Meter meter = meters.get(invocation.getMethod());
				if (meter != null) {
					meter.mark();
				}
			}
			throw t;
		}
	}

	@Override
	public void doWith(Method method) throws IllegalArgumentException, IllegalAccessException {
		final ExceptionMetered annotation = method.getAnnotation(ExceptionMetered.class);
		final String metricName = Util.forExceptionMeteredMethod(targetClass, method, annotation);
		final Meter meter = metrics.meter(metricName);

		meters.put(method, meter);
		causes.put(method, annotation.cause());

		log.debug("Created metric {} for method {}", metricName, method.getName());
	}

	@Override
	public int getOrder() {
		return HIGHEST_PRECEDENCE;
	}

}
