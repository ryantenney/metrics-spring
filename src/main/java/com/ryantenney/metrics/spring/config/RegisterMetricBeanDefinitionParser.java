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
package com.ryantenney.metrics.spring.config;

import java.util.List;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.parsing.BeanComponentDefinition;
import org.springframework.beans.factory.parsing.CompositeComponentDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.MetricSet;

import static com.ryantenney.metrics.spring.config.MetricsNamespaceHandler.METRICS_NAMESPACE;

class RegisterMetricBeanDefinitionParser implements BeanDefinitionParser {

	@Override
	public BeanDefinition parse(Element element, ParserContext parserContext) {
		final CompositeComponentDefinition compDefinition = new CompositeComponentDefinition(element.getTagName(), parserContext.extractSource(element));
		parserContext.pushContainingComponent(compDefinition);

		final String metricRegistryBeanName = element.getAttribute("metric-registry");
		if (!StringUtils.hasText(metricRegistryBeanName)) {
			throw new RuntimeException(); // TODO
		}
		final RuntimeBeanReference metricRegistryBeanRef = new RuntimeBeanReference(metricRegistryBeanName);

		final List<Element> metricElements = DomUtils.getChildElementsByTagName(element, new String[] { "bean", "ref" });
		for (Element metricElement : metricElements) {
			// Get the name attribute and remove it (to prevent Spring from looking for a BeanDefinitionDecorator)
			final String name = metricElement.getAttributeNS(METRICS_NAMESPACE, "name");
			if (name != null) {
				metricElement.removeAttributeNS(METRICS_NAMESPACE, "name");
			}

			final Object metric = parserContext.getDelegate().parsePropertySubElement(metricElement, null);

			final RootBeanDefinition metricRegistererDef = new RootBeanDefinition(MetricRegisterer.class);
			metricRegistererDef.setSource(parserContext.extractSource(metricElement));
			metricRegistererDef.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);

			final ConstructorArgumentValues args = metricRegistererDef.getConstructorArgumentValues();
			args.addIndexedArgumentValue(0, metricRegistryBeanRef);
			args.addIndexedArgumentValue(1, name);
			args.addIndexedArgumentValue(2, metric);

			final String beanName = parserContext.getReaderContext().registerWithGeneratedName(metricRegistererDef);
			parserContext.registerComponent(new BeanComponentDefinition(metricRegistererDef, beanName));
		}

		parserContext.popAndRegisterContainingComponent();

		return null;
	}

	public static class MetricRegisterer implements InitializingBean {

		private final MetricRegistry metricRegistry;
		private final String name;
		private final Metric metric;

		public MetricRegisterer(MetricRegistry metricRegistry, String name, Metric metric) {
			this.metricRegistry = metricRegistry;
			this.name = name;
			this.metric = metric;

			if (!StringUtils.hasText(name) && !(metric instanceof MetricSet)) {
				throw new RuntimeException(); // TODO
			}
		}

		@Override
		public void afterPropertiesSet() throws Exception {
			metricRegistry.register(name, metric);
		}

	}

}
