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

import static io.dropwizard.metrics.MetricRegistry.name;

import java.lang.reflect.Member;

import com.ryantenney.metrics.annotation.CachedGauge;
import com.ryantenney.metrics.annotation.Counted;
import com.ryantenney.metrics.annotation.Metric;

import io.dropwizard.metrics.annotation.ExceptionMetered;
import io.dropwizard.metrics.annotation.Gauge;
import io.dropwizard.metrics.annotation.Metered;
import io.dropwizard.metrics.annotation.Timed;

import io.dropwizard.metrics.MetricName;

class Util {

	private Util() {}

	static MetricName forTimedMethod(Class<?> klass, Member member, Timed annotation) {
		return chooseName(annotation.name(), annotation.absolute(), klass, member);
	}

	static MetricName forMeteredMethod(Class<?> klass, Member member, Metered annotation) {
		return chooseName(annotation.name(), annotation.absolute(), klass, member);
	}

	static MetricName forGauge(Class<?> klass, Member member, Gauge annotation) {
		return chooseName(annotation.name(), annotation.absolute(), klass, member);
	}

	static MetricName forCachedGauge(Class<?> klass, Member member, CachedGauge annotation) {
		return chooseName(annotation.name(), annotation.absolute(), klass, member);
	}

	static MetricName forExceptionMeteredMethod(Class<?> klass, Member member, ExceptionMetered annotation) {
		return chooseName(annotation.name(), annotation.absolute(), klass, member, ExceptionMetered.DEFAULT_NAME_SUFFIX);
	}

	static MetricName forCountedMethod(Class<?> klass, Member member, Counted annotation) {
		return chooseName(annotation.name(), annotation.absolute(), klass, member);
	}

	static MetricName forMetricField(Class<?> klass, Member member, Metric annotation) {
		return chooseName(annotation.name(), annotation.absolute(), klass, member);
	}

	static MetricName chooseName(String explicitName, boolean absolute, Class<?> klass, Member member, String... suffixes) {
		if (explicitName != null && !explicitName.isEmpty()) {
			if (absolute) {
				return MetricName.build(explicitName);
			}
			return name(klass.getCanonicalName(), explicitName);
		}
		return MetricName.join(name(klass.getCanonicalName(), member.getName()), MetricName.build(suffixes));
	}

}
