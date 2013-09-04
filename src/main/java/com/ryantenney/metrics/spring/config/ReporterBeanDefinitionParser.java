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

import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;

import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

import com.ryantenney.metrics.spring.reporter.ReporterElementParser;

/**
 * Has the side effect of registering 'name' as aliases
 */
class ReporterBeanDefinitionParser extends AbstractBeanDefinitionParser {

	private final ServiceLoader<ReporterElementParser> reporterElementParserLoader = ServiceLoader.load(ReporterElementParser.class);

	@Override
	protected AbstractBeanDefinition parseInternal(Element element, ParserContext parserContext) {
		final String metricRegistryRef = element.getAttribute("metric-registry");
		if (!StringUtils.hasText(metricRegistryRef)) {
			parserContext.getReaderContext().error("Metric-registry id required for element '" + element.getLocalName() + "'", element);
			return null;
		}

		final String type = element.getAttribute("type");
		if (!StringUtils.hasText(type)) {
			parserContext.getReaderContext().error("Type required for element '" + element.getLocalName() + "'", element);
			return null;
		}

		try {
			for (ReporterElementParser reporterElementParser : reporterElementParserLoader) {
				if (type.equals(reporterElementParser.getType())) {
					return reporterElementParser.parseReporter(element, parserContext);
				}
			}
		}
		catch (ServiceConfigurationError ex) {
			parserContext.getReaderContext().error("Error loading ReporterElementParsers", element, ex);
			return null;
		}

		parserContext.getReaderContext().error("No ReporterElementParser found for reporter type '" + type + "'", element);
		return null;
	}

	@Override
	protected boolean shouldGenerateIdAsFallback() {
		return true;
	}

}
