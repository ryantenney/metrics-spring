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

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.springframework.util.ReflectionUtils.FieldFilter;
import org.springframework.util.ReflectionUtils.MethodFilter;

class AnnotationFilter implements MethodFilter, FieldFilter {

	private final Class<? extends Annotation> clazz;

	public AnnotationFilter(final Class<? extends Annotation> clazz) {
		this.clazz = clazz;
	}

	@Override
	public boolean matches(Method method) {
		return method.isAnnotationPresent(clazz);
	}

	@Override
	public boolean matches(Field field) {
		return field.isAnnotationPresent(clazz);
	}

}
