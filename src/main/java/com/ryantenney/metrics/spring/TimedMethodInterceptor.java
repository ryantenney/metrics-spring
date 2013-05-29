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
import org.springframework.core.Ordered;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.ReflectionUtils.MethodCallback;
import org.springframework.util.ReflectionUtils.MethodFilter;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.codahale.metrics.annotation.Timed;

class TimedMethodInterceptor implements MethodInterceptor, MethodCallback, Ordered {

	private static final Logger log = LoggerFactory.getLogger(TimedMethodInterceptor.class);

	private static final MethodFilter filter = new AnnotationFilter(Timed.class);

	private final MetricRegistry metrics;
	private final Class<?> targetClass;
	private final Map<MethodKey, Timer> timers;

	public TimedMethodInterceptor(final MetricRegistry metrics, final Class<?> targetClass) {
		this.metrics = metrics;
		this.targetClass = targetClass;
		this.timers = new HashMap<MethodKey, Timer>();

		log.debug("Creating method interceptor for class {}", targetClass.getCanonicalName());
		log.debug("Scanning for @Timed annotated methods");

		ReflectionUtils.doWithMethods(targetClass, this, filter);
	}

	@Override
	public Object invoke(MethodInvocation invocation) throws Throwable {
		final Timer timer = timers.get(MethodKey.forMethod(invocation.getMethod()));
		final com.codahale.metrics.Timer.Context timerCtx = timer != null ? timer.time() : null;
		try {
			return invocation.proceed();
		}
		finally {
			if (timerCtx != null) {
				timerCtx.stop();
			}
		}
	}

	@Override
	public void doWith(Method method) throws IllegalArgumentException, IllegalAccessException {
		final Timed annotation = method.getAnnotation(Timed.class);
		final String metricName = Util.forTimedMethod(targetClass, method, annotation);
		final Timer timer = metrics.timer(metricName);

		timers.put(MethodKey.forMethod(method), timer);

		log.debug("Created metric {} for method {}", metricName, method.getName());
	}

	@Override
	public int getOrder() {
		return HIGHEST_PRECEDENCE;
	}

}
