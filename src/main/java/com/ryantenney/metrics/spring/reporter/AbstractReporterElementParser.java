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
package com.ryantenney.metrics.spring.reporter;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public abstract class AbstractReporterElementParser implements ReporterElementParser {

	protected static final String ID = "id";
	protected static final String TYPE = "type";
	protected static final String METRIC_REGISTRY_REF = "metric-registry";

	protected Class<?> getBeanClass() {
		return null;
	}

	protected String getBeanClassName() {
		return null;
	}

	protected void parseReporter(Element element, BeanDefinitionBuilder beanDefinitionBuilder) {
	}

	@Override
	public AbstractBeanDefinition parseReporter(Element element, ParserContext parserContext) {
		final BeanDefinitionBuilder beanDefBuilder;
		if (getBeanClass() != null) {
			beanDefBuilder = BeanDefinitionBuilder.rootBeanDefinition(getBeanClass());
		}
		else {
			beanDefBuilder = BeanDefinitionBuilder.rootBeanDefinition(getBeanClassName());
		}

		beanDefBuilder.setRole(BeanDefinition.ROLE_APPLICATION);
		beanDefBuilder.getRawBeanDefinition().setSource(parserContext.extractSource(element));
		try {
			parseReporter(element, beanDefBuilder);
			addDefaultProperties(element, beanDefBuilder);
			return beanDefBuilder.getBeanDefinition();
		}
		catch (Exception ex) {
			parserContext.getReaderContext().error(ex.getMessage(), element, ex);
			return null;
		}
	}

	protected void addDefaultProperties(Element element, BeanDefinitionBuilder beanDefBuilder) {
		final Map<String, String> properties = new HashMap<String, String>();
		final NamedNodeMap attributes = element.getAttributes();
		for (int i = 0; i < attributes.getLength(); i++) {
			final Node attribute = attributes.item(i);
			final String name = attribute.getNodeName();
			if (name.equals(METRIC_REGISTRY_REF) || name.equals(ID) || name.equals(TYPE)) {
				continue;
			}
			properties.put(name, attribute.getNodeValue());
		}

		validate(properties);

		beanDefBuilder.addPropertyReference("metricRegistry", element.getAttribute(METRIC_REGISTRY_REF));
		beanDefBuilder.addPropertyValue("properties", properties);
	}

	protected void validate(Map<String, String> properties) {
		final ValidationContext validationContext = new ValidationContext(properties);
		try {
			validate(validationContext);
		}
		catch (ValidationException ex) {
			throw ex;
		}
		catch (Throwable ex) {
			validationContext.reject(ex.getMessage(), ex);
		}
	}

	protected void validate(ValidationContext validator) {
	}

	protected static class ValidationException extends RuntimeException {

		private static final long serialVersionUID = 6435244612757724933L;

		public ValidationException(String message) {
			super(message);
		}

		public ValidationException(String message, Throwable cause) {
			super(message, cause);
		}

	}

	protected static class ValidationContext {

		private String lastKey;
		private final Map<String, String> properties;
		private final Set<String> allowedProperties;

		public ValidationContext(Map<String, String> properties) {
			this.properties = properties;
			this.allowedProperties = new HashSet<String>();
		}

		public boolean has(String key) {
			return properties.get(key) != null;
		}

		public String get(String key) {
			this.lastKey = key;
			return properties.get(key);
		}

		public void reject(String message) {
			reject(lastKey, message);
		}

		public void reject(String message, Throwable cause) {
			reject(lastKey, message, cause);
		}

		public void reject(String key, String message) {
			throw new ValidationException(errorMessage(key, message));
		}

		public void reject(String key, String message, Throwable cause) {
			throw new ValidationException(errorMessage(key, message), cause);
		}

		public String require(String key) {
			return require(key, null, null);
		}

		public String require(String key, String pattern) {
			return require(key, pattern, null);
		}

		public String require(String key, String pattern, String message) {
			final String value = get(key);
			if (!StringUtils.hasText(value)) {
				reject(key, message);
			}
			check(key, value, pattern, message);
			allowedProperties.add(key);
			return value;
		}

		public boolean optional(String key) {
			return optional(key, null, null);
		}

		public boolean optional(String key, String pattern) {
			return optional(key, pattern, null);
		}

		public boolean optional(String key, String pattern, String message) {
			final String value = get(key);
			if (StringUtils.hasText(value)) {
				check(key, value, pattern, message);
				allowedProperties.add(key);
				return true;
			}
			return false;
		}

		public void rejectUnmatchedProperties() {
			if (!allowedProperties.containsAll(properties.keySet())) {
				final Set<String> unmatchedProperties = new HashSet<String>(properties.keySet());
				unmatchedProperties.removeAll(allowedProperties);
				throw new ValidationException("Properties " + Arrays.toString(unmatchedProperties.toArray()) +
						" are not permitted on this element.");
			}
		}

		private String errorMessage(String key, String message) {
			return "Attribute '" + key + "'" + (message != null ? ": " + message : "");
		}

		private void check(String key, String value, String pattern, String message) {
			if (pattern != null && !value.matches(pattern)) {
				reject(key, message);
				return;
			}
		}

	}

}
