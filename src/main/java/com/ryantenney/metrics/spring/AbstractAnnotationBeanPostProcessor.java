/**
 * Copyright © 2012 Ryan W Tenney (ryan@10e.us)
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

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.util.ReflectionUtils.FieldCallback;
import org.springframework.util.ReflectionUtils.MethodCallback;

import static org.springframework.aop.support.AopUtils.getTargetClass;
import static org.springframework.util.ReflectionUtils.doWithFields;
import static org.springframework.util.ReflectionUtils.doWithMethods;

abstract class AbstractAnnotationBeanPostProcessor implements BeanPostProcessor {

	protected final Logger LOG = LoggerFactory.getLogger(getClass());

	public static enum Members {
		FIELDS, METHODS, ALL
	}

	public static enum Phase {
		PRE_INIT, POST_INIT;
	}

	private final Members members;
	private final Phase phase;
	private final AnnotationFilter filter;

	public AbstractAnnotationBeanPostProcessor(final Members members, final Phase phase, final AnnotationFilter filter) {
		this.members = members;
		this.phase = phase;
		this.filter = filter;
	}

	/**
	 * @param bean
	 * @param beanName
	 * @param targetClass
	 * @param field
	 */
	protected void withField(Object bean, String beanName, Class<?> targetClass, Field field) {}

	/**
	 * @param bean
	 * @param beanName
	 * @param targetClass
	 * @param method
	 */
	protected void withMethod(Object bean, String beanName, Class<?> targetClass, Method method) {}

	@Override
	public final Object postProcessBeforeInitialization(Object bean, String beanName) {
		if (phase == Phase.PRE_INIT) {
			process(bean, beanName);
		}

		return bean;
	}

	@Override
	public final Object postProcessAfterInitialization(Object bean, String beanName) {
		if (phase == Phase.POST_INIT) {
			process(bean, beanName);
		}

		return bean;
	}

	private void process(final Object bean, final String beanName) {
		final Class<?> targetClass = getTargetClass(bean);

		if (members == Members.FIELDS || members == Members.ALL) {
			doWithFields(targetClass, new FieldCallback() {
				@Override
				public void doWith(Field field) throws IllegalAccessException {
					withField(bean, beanName, targetClass, field);
				}
			}, filter);
		}

		if (members == Members.METHODS || members == Members.ALL) {
			doWithMethods(targetClass, new MethodCallback() {
				@Override
				public void doWith(final Method method) throws IllegalAccessException {
					withMethod(bean, beanName, targetClass, method);
				}
			}, filter);
		}
	}

}
