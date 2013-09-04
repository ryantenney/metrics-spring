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
package com.ryantenney.metrics.spring.config;

import static org.springframework.beans.factory.config.BeanDefinition.ROLE_APPLICATION;
import static org.springframework.beans.factory.config.BeanDefinition.ROLE_INFRASTRUCTURE;

import org.springframework.aop.framework.ProxyConfig;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.parsing.BeanComponentDefinition;
import org.springframework.beans.factory.parsing.CompositeComponentDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheckRegistry;
import com.ryantenney.metrics.spring.MetricsBeanPostProcessorFactory;

class AnnotationDrivenBeanDefinitionParser implements BeanDefinitionParser {

	@Override
	public BeanDefinition parse(Element element, ParserContext parserContext) {
		final Object source = parserContext.extractSource(element);

		final CompositeComponentDefinition compDefinition = new CompositeComponentDefinition(element.getTagName(), source);
		parserContext.pushContainingComponent(compDefinition);

		String metricsBeanName = element.getAttribute("metric-registry");
		if (!StringUtils.hasText(metricsBeanName)) {
			metricsBeanName = registerComponent(parserContext, build(MetricRegistry.class, source, ROLE_APPLICATION));
		}

		String healthCheckBeanName = element.getAttribute("health-check-registry");
		if (!StringUtils.hasText(healthCheckBeanName)) {
			healthCheckBeanName = registerComponent(parserContext, build(HealthCheckRegistry.class, source, ROLE_APPLICATION));
		}

		final ProxyConfig proxyConfig = new ProxyConfig();

		if (StringUtils.hasText(element.getAttribute("expose-proxy"))) {
			proxyConfig.setExposeProxy(Boolean.valueOf(element.getAttribute("expose-proxy")));
		}

		if (StringUtils.hasText(element.getAttribute("proxy-target-class"))) {
			proxyConfig.setProxyTargetClass(Boolean.valueOf(element.getAttribute("proxy-target-class")));
		}

		//@formatter:off

		registerComponent(parserContext,
				build(MetricsBeanPostProcessorFactory.class, source, ROLE_INFRASTRUCTURE)
					.setFactoryMethod("exceptionMetered")
					.addConstructorArgReference(metricsBeanName)
					.addConstructorArgValue(proxyConfig));

		registerComponent(parserContext,
				build(MetricsBeanPostProcessorFactory.class, source, ROLE_INFRASTRUCTURE)
					.setFactoryMethod("metered")
					.addConstructorArgReference(metricsBeanName)
					.addConstructorArgValue(proxyConfig));

		registerComponent(parserContext,
				build(MetricsBeanPostProcessorFactory.class, source, ROLE_INFRASTRUCTURE)
					.setFactoryMethod("timed")
					.addConstructorArgReference(metricsBeanName)
					.addConstructorArgValue(proxyConfig));

		registerComponent(parserContext,
				build(MetricsBeanPostProcessorFactory.class, source, ROLE_INFRASTRUCTURE)
					.setFactoryMethod("counted")
					.addConstructorArgReference(metricsBeanName)
					.addConstructorArgValue(proxyConfig));

		registerComponent(parserContext,
				build(MetricsBeanPostProcessorFactory.class, source, ROLE_INFRASTRUCTURE)
					.setFactoryMethod("gauge")
					.addConstructorArgReference(metricsBeanName));

		registerComponent(parserContext,
				build(MetricsBeanPostProcessorFactory.class, source, ROLE_INFRASTRUCTURE)
					.setFactoryMethod("injectMetric")
					.addConstructorArgReference(metricsBeanName));

		registerComponent(parserContext,
				build(MetricsBeanPostProcessorFactory.class, source, ROLE_INFRASTRUCTURE)
					.setFactoryMethod("healthCheck")
					.addConstructorArgReference(healthCheckBeanName));

		//@formatter:on

		parserContext.popAndRegisterContainingComponent();

		return null;
	}

	private BeanDefinitionBuilder build(Class<?> klazz, Object source, int role) {
		final BeanDefinitionBuilder beanDefBuilder = BeanDefinitionBuilder.rootBeanDefinition(klazz);
		beanDefBuilder.setRole(role);
		beanDefBuilder.getRawBeanDefinition().setSource(source);
		return beanDefBuilder;
	}

	private String registerComponent(ParserContext parserContext, BeanDefinitionBuilder beanDefBuilder) {
		final BeanDefinition beanDef = beanDefBuilder.getBeanDefinition();
		final String beanName = parserContext.getReaderContext().registerWithGeneratedName(beanDef);
		parserContext.registerComponent(new BeanComponentDefinition(beanDef, beanName));
		return beanName;
	}

}
