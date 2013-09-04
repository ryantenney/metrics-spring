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

import static com.codahale.metrics.MetricRegistry.name;

import java.lang.reflect.Member;

import com.codahale.metrics.annotation.ExceptionMetered;
import com.codahale.metrics.annotation.Gauge;
import com.codahale.metrics.annotation.Metered;
import com.codahale.metrics.annotation.Timed;
import com.ryantenney.metrics.annotation.Counted;
import com.ryantenney.metrics.annotation.InjectMetric;

class Util {

	private Util() {
	}

	static String forTimedMethod(Class<?> klass, Member member, Timed annotation) {
		return chooseName(annotation.name(), annotation.absolute(), klass, member);
	}

	static String forMeteredMethod(Class<?> klass, Member member, Metered annotation) {
		return chooseName(annotation.name(), annotation.absolute(), klass, member);
	}

	static String forGauge(Class<?> klass, Member member, Gauge annotation) {
		return chooseName(annotation.name(), annotation.absolute(), klass, member);
	}

	static String forExceptionMeteredMethod(Class<?> klass, Member member, ExceptionMetered annotation) {
		return chooseName(annotation.name(), annotation.absolute(), klass, member, ExceptionMetered.DEFAULT_NAME_SUFFIX);
	}

	static String forCountedMethod(Class<?> klass, Member member, Counted annotation) {
		return chooseName(annotation.name(), annotation.absolute(), klass, member);
	}

	static String forInjectMetricField(Class<?> klass, Member member, InjectMetric annotation) {
		return chooseName(annotation.name(), annotation.absolute(), klass, member);
	}

	static String chooseName(String explicitName, boolean absolute, Class<?> klass, Member member, String... suffixes) {
		if (explicitName != null && !explicitName.isEmpty()) {
			if (absolute) {
				return explicitName;
			}
			return name(klass.getCanonicalName(), explicitName);
		}
		return name(name(klass.getCanonicalName(), member.getName()), suffixes);
	}

}

