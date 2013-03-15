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
package com.ryantenney.metrics.spring.config;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.w3c.dom.Element;

import com.ryantenney.metrics.spring.GraphiteReporterFactory;

class GraphiteReporterBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {

	@Override
	protected Class<?> getBeanClass(Element element) {
		return GraphiteReporterFactory.class;
	}

	@Override
	protected boolean shouldGenerateIdAsFallback() {
		return true;
	}

	@Override
	protected void doParse(Element element, BeanDefinitionBuilder builder) {
		builder.setFactoryMethod("createInstance");
		builder.addConstructorArgReference(element.getAttribute("metrics-registry"));
		builder.addConstructorArgValue(element.getAttribute("graphite-host"));
		builder.addConstructorArgValue(Integer.parseInt(element.getAttribute("graphite-port")));
		if(element.getAttribute("prefix") != null) {
			builder.addConstructorArgValue(element.getAttribute("prefix"));
		}
		builder.addConstructorArgValue(Integer.parseInt(element.getAttribute("report-interval-seconds")));
	}

}
