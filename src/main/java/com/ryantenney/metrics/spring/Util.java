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

import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Field;

import com.ryantenney.metrics.annotation.InjectedMetric;
import com.yammer.metrics.annotation.*;
import com.yammer.metrics.core.MetricName;

class Util {

	private static String getPackageName(Class<?> klass) {
		return klass.getPackage() == null ? "" : klass.getPackage().getName();
	}

	private static String getClassName(Class<?> klass) {
		return klass.getSimpleName().replaceAll("\\$$", "");
	}

	private static String chooseDomain(String domain, Class<?> klass) {
		if(domain == null || domain.isEmpty()) {
			domain = getPackageName(klass);
		}
		return domain;
	}

	private static String chooseType(String type, Class<?> klass) {
		if(type == null || type.isEmpty()) {
			type = getClassName(klass);
		}
		return type;
	}

	private static String chooseName(String name, Member field) {
		if(name == null || name.isEmpty()) {
			name = field.getName();
		}
		return name;
	}

	public static MetricName forTimedMethod(Class<?> klass, Method method, Timed annotation, String scope) {
		return new MetricName(chooseDomain(annotation.group(), klass),
							  chooseType(annotation.type(), klass),
							  chooseName(annotation.name(), method),
							  scope);
	}

	public static MetricName forMeteredMethod(Class<?> klass, Method method, Metered annotation, String scope) {
		return new MetricName(chooseDomain(annotation.group(), klass),
							  chooseType(annotation.type(), klass),
							  chooseName(annotation.name(), method),
							  scope);
	}

	public static MetricName forGauge(Class<?> klass, Member member, Gauge annotation, String scope) {
		return new MetricName(chooseDomain(annotation.group(), klass),
							  chooseType(annotation.type(), klass),
							  chooseName(annotation.name(), member),
							  scope);
	}

	public static MetricName forExceptionMeteredMethod(Class<?> klass, Method method, ExceptionMetered annotation, String scope) {
		return new MetricName(chooseDomain(annotation.group(), klass),
							  chooseType(annotation.type(), klass),
							  annotation.name().isEmpty()
								  ? method.getName() + ExceptionMetered.DEFAULT_NAME_SUFFIX
								  : annotation.name(),
							  scope);
	}

	public static MetricName forInjectedMetricField(Class<?> klass, Field field, InjectedMetric annotation, String scope) {
		return new MetricName(chooseDomain(annotation.group(), klass),
							  chooseType(annotation.type(), klass),
							  chooseName(annotation.name(), field),
							  scope);
	}

}
