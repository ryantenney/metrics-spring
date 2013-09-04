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

import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.SharedMetricRegistries;

/**
 * Has the side effect of registering 'name' as aliases
 */
class MetricRegistryBeanDefinitionParser extends AbstractBeanDefinitionParser {

	@Override
	protected AbstractBeanDefinition parseInternal(Element element, ParserContext parserContext) {
		final Object source = parserContext.extractSource(element);
		final String name = element.getAttribute("name");
		if (StringUtils.hasText(name)) {
			final BeanDefinitionBuilder beanDefBuilder = build(SharedMetricRegistries.class, source);
			beanDefBuilder.setFactoryMethod("getOrCreate");
			beanDefBuilder.addConstructorArgValue(name);
			return beanDefBuilder.getBeanDefinition();
		}
		else {
			return build(MetricRegistry.class, source).getBeanDefinition();
		}
	}

	@Override
	protected boolean shouldGenerateIdAsFallback() {
		return true;
	}

	private BeanDefinitionBuilder build(Class<?> klazz, Object source) {
		final BeanDefinitionBuilder beanDefBuilder = BeanDefinitionBuilder.rootBeanDefinition(klazz);
		beanDefBuilder.setRole(ROLE_APPLICATION);
		beanDefBuilder.getRawBeanDefinition().setSource(source);
		return beanDefBuilder;
	}

}
