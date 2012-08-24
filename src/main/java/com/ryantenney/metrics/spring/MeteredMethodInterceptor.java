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

import com.yammer.metrics.annotation.Metered;
import com.yammer.metrics.core.Meter;
import com.yammer.metrics.core.MetricName;
import com.yammer.metrics.core.MetricsRegistry;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.ReflectionUtils.MethodCallback;
import org.springframework.util.ReflectionUtils.MethodFilter;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

class MeteredMethodInterceptor implements MethodInterceptor, MethodCallback {

	private static final Logger log = LoggerFactory.getLogger(MeteredMethodInterceptor.class);

	private static final MethodFilter filter = new AnnotationFilter(Metered.class);

	protected final MetricsRegistry metrics;
	protected final Class<?> targetClass;
	protected final Map<String, Meter> meters;
	protected final String scope;

	public MeteredMethodInterceptor(final MetricsRegistry metrics, final Class<?> targetClass, final String scope) {
		this.metrics = metrics;
		this.targetClass = targetClass;
		this.meters = new HashMap<String, Meter>();
		this.scope = scope;

		log.debug("Creating method interceptor for class {}", targetClass.getCanonicalName());
		log.debug("Scanning for @Metered annotated methods");

		ReflectionUtils.doWithMethods(targetClass, this, filter);
	}

	@Override
	public Object invoke(MethodInvocation invocation) throws Throwable {
		Meter meter = meters.get(invocation.getMethod().getName());
		if (meter != null) {
			meter.mark();
		}
		return invocation.proceed();
	}

	@Override
	public void doWith(Method method) throws IllegalArgumentException, IllegalAccessException {
		final Metered annotation = method.getAnnotation(Metered.class);
		final MetricName metricName = Util.forMeteredMethod(targetClass, method, annotation, scope);
		final Meter meter = metrics.newMeter(metricName, annotation.eventType(), annotation.rateUnit());

		meters.put(method.getName(), meter);

		log.debug("Created metric {} for method {}", metricName, method.getName());
	}

}
