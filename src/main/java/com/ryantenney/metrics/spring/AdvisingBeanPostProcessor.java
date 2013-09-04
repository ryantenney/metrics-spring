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

import org.aopalliance.aop.Advice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.Advisor;
import org.springframework.aop.Pointcut;
import org.springframework.aop.framework.Advised;
import org.springframework.aop.framework.AopInfrastructureBean;
import org.springframework.aop.framework.ProxyConfig;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.util.ClassUtils;

class AdvisingBeanPostProcessor implements BeanPostProcessor {

	private static final Logger LOG = LoggerFactory.getLogger(AdvisingBeanPostProcessor.class);

	private final ClassLoader beanClassLoader = ClassUtils.getDefaultClassLoader();

	private final Pointcut pointcut;
	private final AdviceFactory adviceFactory;
	private final ProxyConfig proxyConfig;

	public AdvisingBeanPostProcessor(final Pointcut pointcut, final AdviceFactory adviceFactory, final ProxyConfig proxyConfig) {
		this.pointcut = pointcut;
		this.adviceFactory = adviceFactory;
		this.proxyConfig = proxyConfig;
	}

	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName) {
		return bean;
	}

	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) {
		if (bean instanceof AopInfrastructureBean) {
			return bean;
		}

		final Class<?> targetClass = AopUtils.getTargetClass(bean);

		if (AopUtils.canApply(pointcut, targetClass)) {
			final Advice advice = adviceFactory.getAdvice(bean, targetClass);
			final Advisor advisor = new DefaultPointcutAdvisor(pointcut, advice);

			if (bean instanceof Advised) {
				LOG.debug("Bean {} is already proxied, adding Advisor to existing proxy", beanName);

				((Advised) bean).addAdvisor(0, advisor);

				return bean;
			}
			else {
				LOG.debug("Proxying bean {} of type {}", beanName, targetClass.getCanonicalName());

				final ProxyFactory proxyFactory = new ProxyFactory(bean);
				if (proxyConfig != null) {
					proxyFactory.copyFrom(proxyConfig);
				}
				proxyFactory.addAdvisor(advisor);

				final Object proxy = proxyFactory.getProxy(this.beanClassLoader);

				return proxy;
			}
		}

		return bean;
	}

}
