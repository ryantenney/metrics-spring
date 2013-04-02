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

import com.ryantenney.metrics.annotation.InjectedMetric;
import com.yammer.metrics.annotation.*;
import org.slf4j.LoggerFactory;

import static com.yammer.metrics.MetricRegistry.name;

class Util {

	public static String forTimedMethod(Class<?> klass, Member member, Timed annotation) {
        return chooseName(annotation.name(), annotation.absolute(), klass, member);
	}

	public static String forMeteredMethod(Class<?> klass, Member member, Metered annotation) {
        return chooseName(annotation.name(), annotation.absolute(), klass, member);
	}

	public static String forGauge(Class<?> klass, Member member, Gauge annotation) {
        return chooseName(annotation.name(), annotation.absolute(), klass, member);
	}

	public static String forExceptionMeteredMethod(Class<?> klass, Member member, ExceptionMetered annotation) {
		return chooseName(annotation.name(), annotation.absolute(), klass, member, ExceptionMetered.DEFAULT_NAME_SUFFIX);
	}

	public static String forInjectedMetricField(Class<?> klass, Member member, InjectedMetric annotation) {
		return chooseName(annotation.name(), annotation.absolute(), klass, member);
	}

    private static String chooseName(String explicitName, boolean absolute, Class<?> klass, Member member, String... suffixes) {
        if (explicitName != null && !explicitName.isEmpty()) {
            if (absolute) return explicitName;
            return name(klass.getCanonicalName(), explicitName);
        }
        return name(name(klass.getCanonicalName(), member.getName()), suffixes);
    }

}