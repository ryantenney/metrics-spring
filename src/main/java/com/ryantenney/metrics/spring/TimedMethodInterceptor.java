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

import static com.ryantenney.metrics.spring.AnnotationFilter.PROXYABLE_METHODS;

import java.lang.annotation.Annotation;

import org.aopalliance.aop.Advice;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.core.Ordered;

import io.dropwizard.metrics.MetricRegistry;
import io.dropwizard.metrics.Timer;
import io.dropwizard.metrics.Timer.Context;

class TimedMethodInterceptor<A extends Annotation> extends AbstractMetricMethodInterceptor<A, Timer> implements Ordered {

	public TimedMethodInterceptor(final MetricRegistry metricRegistry, final Class<?> targetClass, Class<A> annotationClass, MetricFactory<Timer, A> timerFactory, MetricNamingStrategy<A> namingStrategy) {
		super(metricRegistry, targetClass, annotationClass, new AnnotationFilter(annotationClass, PROXYABLE_METHODS), timerFactory, namingStrategy);
	}

	@Override
	protected Object invoke(MethodInvocation invocation, Timer timer, A annotation) throws Throwable {
		final Context timerCtx = timer.time();
		try {
			return invocation.proceed();
		}
		finally {
			timerCtx.close();
		}
	}

	@Override
	public int getOrder() {
		return HIGHEST_PRECEDENCE;
	}

	static <A extends Annotation> AdviceFactory adviceFactory(final MetricRegistry metricRegistry, final Class<A> annotationClass, 
			final MetricFactory<Timer, A> timerFactory, final MetricNamingStrategy<A> namingStrategy) {
		return new AdviceFactory() {
			@Override
			public Advice getAdvice(Object bean, Class<?> targetClass) {
				return new TimedMethodInterceptor<>(metricRegistry, targetClass, annotationClass, timerFactory, namingStrategy);
			}
		};
	}
}
