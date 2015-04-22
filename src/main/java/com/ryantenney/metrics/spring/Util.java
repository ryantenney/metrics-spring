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

import java.lang.annotation.Annotation;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.Collection;

import com.codahale.metrics.annotation.ExceptionMetered;
import com.codahale.metrics.annotation.Gauge;
import com.codahale.metrics.annotation.Metered;
import com.codahale.metrics.annotation.Timed;
import com.ryantenney.metrics.annotation.CachedGauge;
import com.ryantenney.metrics.annotation.Counted;
import com.ryantenney.metrics.annotation.Metric;
import com.ryantenney.metrics.annotation.MetricParam;
import org.springframework.util.StringUtils;

class Util {

	private Util() {}

	static String forTimedMethod(Class<?> klass, Member member, Timed annotation, Object...arguments) {
		return chooseName(annotation.name(), annotation.absolute(), klass, member, arguments);
	}

	static String forMeteredMethod(Class<?> klass, Member member, Metered annotation, Object...arguments) {
		return chooseName(annotation.name(), annotation.absolute(), klass, member, arguments);
	}

	static String forGauge(Class<?> klass, Member member, Gauge annotation) {
		return chooseName(annotation.name(), annotation.absolute(), klass, member);
	}

	static String forCachedGauge(Class<?> klass, Member member, CachedGauge annotation) {
		return chooseName(annotation.name(), annotation.absolute(), klass, member);
	}

	static String forExceptionMeteredMethod(Class<?> klass, Member member, ExceptionMetered annotation, Object...arguments) {
		return chooseName(annotation.name(), annotation.absolute(), klass, member, ExceptionMetered.DEFAULT_NAME_SUFFIX, arguments);
	}

	static String forCountedMethod(Class<?> klass, Member member, Counted annotation, Object...arguments) {
		return chooseName(annotation.name(), annotation.absolute(), klass, member, arguments);
	}

	static String forMetricField(Class<?> klass, Member member, Metric annotation) {
		return chooseName(annotation.name(), annotation.absolute(), klass, member);
	}

    static String chooseName(String explicitName, boolean absolute, Class<?> klass, Member member, String suffix, Object...arguments) {
        return name(chooseName(explicitName, absolute, klass, member, arguments), suffix);
    }

	static String chooseName(String explicitName, boolean absolute, Class<?> klass, Member member, Object...arguments) {
		if (explicitName != null && !explicitName.isEmpty()) {
            //if a method annotation, check arguments for @MetricParam and substitute values in explicitName
            String resolvedName = "";
            if (member instanceof Method) {
                resolvedName = resolveName(explicitName, (Method)member, arguments);
            }
            else {
                resolvedName = explicitName;
            }
            if (absolute) {
				return resolvedName;
			}
			return name(klass.getCanonicalName(), resolvedName);
		}
		return name(name(klass.getCanonicalName(), member.getName()));
	}

    static String resolveName(String explicitName, Method method, Object...arguments) {
        //single arg methods where the arg is null need to be initialized with one null element, since the array comes in as null sometimes
        Object[] args;
        if (arguments == null) {
            args = new Object[]{ null };
        }
        else {
            args = arguments;
        }
        String resolvedName = explicitName;
        MetricParam mp;
        int i = 0;
        Annotation[][] parametersAnnotations = method.getParameterAnnotations();
        for (Annotation[] parameterAnnotations : parametersAnnotations) {
            for (Annotation parameterAnnotation : parameterAnnotations) {
                if (parameterAnnotation != null && parameterAnnotation instanceof MetricParam && i < args.length) {
                    mp = (MetricParam)parameterAnnotation;
                    String token = mp.value();
                    if (StringUtils.isEmpty(token)) {
                        token = Integer.toString(i);
                    }
                    token = "{"+token+"}";
                    String value= "";
                    if (mp.collection()) {
                        int size = 0;
                        if (args[i] != null) {
                            size = ((Collection<?>)args[i]).size();
                        }
                        value = "size." + Integer.toString(size);
                    }
                    else {
                        if (args[i] != null) {
                            value = args[i].toString();
                        }
                        else {
                            value = "null";
                        }
                    }
                    resolvedName = explicitName.replace(token, value);
                }
            }
            i++;
        }
        return resolvedName;
    }

}
