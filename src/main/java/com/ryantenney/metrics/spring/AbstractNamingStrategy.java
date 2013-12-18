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

import com.codahale.metrics.annotation.ExceptionMetered;
import com.codahale.metrics.annotation.Gauge;
import com.codahale.metrics.annotation.Metered;
import com.codahale.metrics.annotation.Timed;
import com.ryantenney.metrics.annotation.CachedGauge;
import com.ryantenney.metrics.annotation.Counted;
import com.ryantenney.metrics.annotation.Metric;

import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;

public abstract class AbstractNamingStrategy implements NamingStrategy {

	@Override
	public String forTimedMethod(Class<?> klass, String beanName, Method method, Timed annotation) {
		return chooseName(annotation.name(), annotation.absolute(), klass, beanName, method);
	}

	@Override
	public String forMeteredMethod(Class<?> klass, String beanName, Method method, Metered annotation) {
		return chooseName(annotation.name(), annotation.absolute(), klass, beanName, method);
	}

	@Override
	public String forExceptionMeteredMethod(Class<?> klass, String beanName, Method method, ExceptionMetered annotation) {
		return chooseName(annotation.name(), annotation.absolute(), klass, beanName, method, ExceptionMetered.DEFAULT_NAME_SUFFIX);
	}

	@Override
	public String forCountedMethod(Class<?> klass, String beanName, Method method, Counted annotation) {
		return chooseName(annotation.name(), annotation.absolute(), klass, beanName, method);
	}

	@Override
	public String forGaugeMethod(Class<?> klass, String beanName, Method method, Gauge annotation) {
		return chooseName(annotation.name(), annotation.absolute(), klass, beanName, method);
	}

	@Override
	public String forGaugeField(Class<?> klass, String beanName, Field field, Gauge annotation) {
		return chooseName(annotation.name(), annotation.absolute(), klass, beanName, field);
	}

	@Override
	public String forCachedGaugeMethod(Class<?> klass, String beanName, Method method, CachedGauge annotation) {
		return chooseName(annotation.name(), annotation.absolute(), klass, beanName, method);
	}

	@Override
	public String forMetricField(Class<?> klass, String beanName, Field field, Metric annotation) {
		return chooseName(annotation.name(), annotation.absolute(), klass, beanName, field);
	}

	protected abstract String chooseName(String explicitName, boolean absolute, Class<?> klass, String beanName, Member member, String... suffixes);

}
