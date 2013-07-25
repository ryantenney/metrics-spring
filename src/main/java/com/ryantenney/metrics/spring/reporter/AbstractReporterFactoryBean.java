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

import java.util.Map;
import java.util.regex.Pattern;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.support.DefaultConversionService;

import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;

public abstract class AbstractReporterFactoryBean<T> implements FactoryBean<T>, InitializingBean, BeanFactoryAware {

	protected static final String FILTER_PATTERN = "filter";
	protected static final String FILTER_REF = "filter-ref";

	private MetricRegistry metricRegistry;
	private BeanFactory beanFactory;
	private ConversionService conversionService;

	private Map<String, String> properties;
	private T instance;

	private boolean initialized = false;

	public abstract Class<? extends T> getObjectType();

	@Override
	public boolean isSingleton() {
		return true;
	}

	@Override
	public T getObject() {
		if (!this.initialized) {
			throw new IllegalStateException("Singleton instance not initialized yet");
		}
		return this.instance;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		this.instance = createInstance();
		this.initialized = true;
	}

	protected abstract T createInstance() throws Exception;

	@Override
	public void setBeanFactory(final BeanFactory beanFactory) {
		this.beanFactory = beanFactory;
		if (beanFactory instanceof ConfigurableBeanFactory) {
			this.conversionService = ((ConfigurableBeanFactory) beanFactory).getConversionService();
		}
	}

	public BeanFactory getBeanFactory() {
		return this.beanFactory;
	}

	public ConversionService getConversionService() {
		if (this.conversionService == null) {
			this.conversionService = new DefaultConversionService();
		}
		return this.conversionService;
	}

	public void setMetricRegistry(final MetricRegistry metricRegistry) {
		this.metricRegistry = metricRegistry;
	}

	public MetricRegistry getMetricRegistry() {
		return metricRegistry;
	}

	public void setProperties(final Map<String, String> properties) {
		this.properties = properties;
	}

	public Map<String, String> getProperties() {
		return properties;
	}

	protected boolean hasProperty(String key) {
		return getProperty(key) != null;
	}

	protected String getProperty(String key) {
		return this.properties.get(key);
	}

	protected String getProperty(String key, String defaultValue) {
		final String value = this.properties.get(key);
		if (value == null) {
			return defaultValue;
		}
		return value;
	}

	protected <V> V getProperty(String key, Class<V> requiredType) {
		return getProperty(key, requiredType, null);
	}

	@SuppressWarnings("unchecked")
	protected <V> V getProperty(String key, Class<V> requiredType, V defaultValue) {
		final String value = this.properties.get(key);
		if (value == null) {
			return defaultValue;
		}
		return (V) getConversionService().convert(value, TypeDescriptor.forObject(value), TypeDescriptor.valueOf(requiredType));
	}

	protected Object getPropertyRef(String key) {
		return getPropertyRef(key, null);
	}

	protected <V> V getPropertyRef(String key, Class<V> requiredType) {
		final String value = this.properties.get(key);
		if (value == null) {
			return null;
		}
		return this.beanFactory.getBean(value, requiredType);
	}

	protected MetricFilter getMetricFilter() {
		if (hasProperty(FILTER_PATTERN)) {
			return metricFilterPattern(getProperty(FILTER_PATTERN));
		}
		else if (hasProperty(FILTER_REF)) {
			return getPropertyRef(FILTER_REF, MetricFilter.class);
		}
		return MetricFilter.ALL;
	}

	protected MetricFilter metricFilterPattern(String pattern) {
		final Pattern filter = Pattern.compile(pattern);
		return new MetricFilter() {

			@Override
			public boolean matches(String name, Metric metric) {
				return filter.matcher(name).matches();
			}

			@Override
			public String toString() {
				return "[MetricFilter regex=" + filter.pattern() + "]";
			}

		};
	}

}
