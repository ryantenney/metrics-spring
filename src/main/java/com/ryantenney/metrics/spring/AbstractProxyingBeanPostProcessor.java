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

import org.aopalliance.intercept.MethodInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.Pointcut;
import org.springframework.aop.PointcutAdvisor;
import org.springframework.aop.framework.Advised;
import org.springframework.aop.framework.AopInfrastructureBean;
import org.springframework.aop.framework.ProxyConfig;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.util.ClassUtils;

abstract class AbstractProxyingBeanPostProcessor extends ProxyConfig implements BeanPostProcessor {

	private static final long serialVersionUID = -3482052668071169769L;

	private final Logger log = LoggerFactory.getLogger(getClass());

	private final ClassLoader beanClassLoader = ClassUtils.getDefaultClassLoader();

	public abstract MethodInterceptor getMethodInterceptor(Class<?> targetClass);

	public abstract Pointcut getPointcut();

	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
		return bean;
	}

	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		if (bean instanceof AopInfrastructureBean) {
			return bean;
		}

		final Class<?> targetClass = AopUtils.getTargetClass(bean);
		final Pointcut pointcut = getPointcut();

		if (AopUtils.canApply(pointcut, targetClass)) {
			final MethodInterceptor interceptor = getMethodInterceptor(targetClass);
			final PointcutAdvisor advisor = new DefaultPointcutAdvisor(pointcut, interceptor);

			if (bean instanceof Advised) {
				log.debug("Bean {} is already proxied, adding Advisor to existing proxy", beanName);

				((Advised) bean).addAdvisor(0, advisor);
				return bean;
			}

			log.debug("Proxying bean {} of type {}", beanName, targetClass.getCanonicalName());

			final ProxyFactory proxyFactory = new ProxyFactory(bean);
			proxyFactory.copyFrom(this);
			proxyFactory.addAdvisor(advisor);

			return proxyFactory.getProxy(this.beanClassLoader);
		}

		return bean;
	}

}
