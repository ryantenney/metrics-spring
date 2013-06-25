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
package com.ryantenney.metrics.spring.config.annotation;

import org.springframework.aop.framework.ProxyConfig;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ImportAware;
import org.springframework.context.annotation.Role;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.Assert;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheckRegistry;
import com.ryantenney.metrics.spring.MetricsBeanPostProcessorFactory;

/**
 * This is the main class providing the configuration behind the Metrics Java config.
 * It is typically imported by adding {@link EnableMetrics @EnableMetrics} to an
 * application {@link Configuration @Configuration} class.
 *
 * @see MetricsConfigurer
 * @see MetricsConfigurerAdapter
 * @author Ryan Tenney
 * @since 3.0
 */
public class MetricsConfigurationSupport implements ImportAware {

	private final Object lock = new Object();

	private volatile MetricRegistry metricRegistry;
	private volatile HealthCheckRegistry healthCheckRegistry;

	private ProxyConfig proxyConfig;

	@Override
	public void setImportMetadata(AnnotationMetadata importMetadata) {
		final AnnotationAttributes enableMetrics = AnnotationAttributes.fromMap(
				importMetadata.getAnnotationAttributes(EnableMetrics.class.getName(), false));
		Assert.notNull(enableMetrics, "@" + EnableMetrics.class.getSimpleName() +
				" is not present on importing class " + importMetadata.getClassName());

		this.proxyConfig = new ProxyConfig();
		this.proxyConfig.setExposeProxy(enableMetrics.getBoolean("exposeProxy"));
		this.proxyConfig.setProxyTargetClass(enableMetrics.getBoolean("proxyTargetClass"));
	}

	@Bean
	@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
	public BeanPostProcessor exceptionMeteredAnnotationBeanPostProcessor() {
		return MetricsBeanPostProcessorFactory.exceptionMetered(getMetricRegistry(), proxyConfig);
	}

	@Bean
	@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
	public BeanPostProcessor meteredAnnotationBeanPostProcessor() {
		return MetricsBeanPostProcessorFactory.metered(getMetricRegistry(), proxyConfig);
	}

	@Bean
	@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
	public BeanPostProcessor timedAnnotationBeanPostProcessor() {
		return MetricsBeanPostProcessorFactory.timed(getMetricRegistry(), proxyConfig);
	}

	@Bean
	@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
	public BeanPostProcessor gaugeAnnotationBeanPostProcessor() {
		return MetricsBeanPostProcessorFactory.gauge(getMetricRegistry());
	}

	@Bean
	@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
	public BeanPostProcessor injectMetricAnnotationBeanPostProcessor() {
		return MetricsBeanPostProcessorFactory.injectMetric(getMetricRegistry());
	}

	@Bean
	@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
	public BeanPostProcessor healthCheckBeanPostProcessor() {
		return MetricsBeanPostProcessorFactory.healthCheck(getHealthCheckRegistry());
	}

	protected MetricRegistry getMetricRegistry() {
		if (metricRegistry == null) {
			synchronized (lock) {
				if (metricRegistry == null) {
					metricRegistry = new MetricRegistry();
				}
			}
		}
		return metricRegistry;
	}

	protected HealthCheckRegistry getHealthCheckRegistry() {
		if (healthCheckRegistry == null) {
			synchronized (lock) {
				if (healthCheckRegistry == null) {
					healthCheckRegistry = new HealthCheckRegistry();
				}
			}
		}
		return healthCheckRegistry;
	}

}
