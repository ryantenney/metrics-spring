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
import org.springframework.util.ReflectionUtils;
import org.springframework.util.ReflectionUtils.MethodCallback;
import org.springframework.util.ReflectionUtils.MethodFilter;

import com.codahale.metrics.Counter;
import com.codahale.metrics.MetricRegistry;
import com.ryantenney.metrics.annotation.Counted;

class CountedMethodInterceptor implements MethodInterceptor, MethodCallback {

	private static final Logger LOG = LoggerFactory.getLogger(CountedMethodInterceptor.class);

	public static final Pointcut POINTCUT = new AnnotationMatchingPointcut(null, Counted.class);
	public static final MethodFilter METHOD_FILTER = new AnnotationFilter(Counted.class);

	private final MetricRegistry metrics;
	private final Class<?> targetClass;
	private final Map<MethodKey, Counter> meters;

	public CountedMethodInterceptor(final MetricRegistry metrics, final Class<?> targetClass) {
		this.metrics = metrics;
		this.targetClass = targetClass;
		this.meters = new HashMap<MethodKey, Counter>();

		LOG.debug("Creating method interceptor for class {}", targetClass.getCanonicalName());
		LOG.debug("Scanning for @Counted annotated methods");

		ReflectionUtils.doWithMethods(targetClass, this, METHOD_FILTER);
	}

	@Override
	public Object invoke(MethodInvocation invocation) throws Throwable {
		final Counter counter = meters.get(MethodKey.forMethod(invocation.getMethod()));
		if (counter != null) {
			counter.inc();
		}
		try {
			return invocation.proceed();
		}
		finally {
			if (counter != null) {
				counter.dec();
			}
		}
	}

	@Override
	public void doWith(Method method) throws IllegalAccessException {
		final Counted annotation = method.getAnnotation(Counted.class);
		final String metricName = Util.forCountedMethod(targetClass, method, annotation);
		final MethodKey methodKey = MethodKey.forMethod(method);

		meters.put(methodKey, metrics.counter(metricName));

		LOG.debug("Created Counter {} for method {}", metricName, methodKey);
	}

}
