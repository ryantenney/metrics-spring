package com.ryantenney.metrics.spring.config.annotation;

import org.springframework.aop.framework.ProxyConfig;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ImportAware;
import org.springframework.context.annotation.Role;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import com.ryantenney.metrics.spring.ExceptionMeteredAnnotationBeanPostProcessor;
import com.ryantenney.metrics.spring.GaugeAnnotationBeanPostProcessor;
import com.ryantenney.metrics.spring.HealthCheckBeanPostProcessor;
import com.ryantenney.metrics.spring.InjectedMetricAnnotationBeanPostProcessor;
import com.ryantenney.metrics.spring.MeteredAnnotationBeanPostProcessor;
import com.ryantenney.metrics.spring.TimedAnnotationBeanPostProcessor;
import com.yammer.metrics.HealthChecks;
import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.HealthCheckRegistry;
import com.yammer.metrics.core.MetricsRegistry;

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

	protected ProxyConfig config;
	protected String scope;

	@Override
	public void setImportMetadata(AnnotationMetadata importMetadata) {
		AnnotationAttributes enableMetrics = AnnotationAttributes.fromMap(importMetadata.getAnnotationAttributes(EnableMetrics.class.getName(), false));
		Assert.notNull(enableMetrics, "@" + EnableMetrics.class.getSimpleName() + " is not present on importing class " + importMetadata.getClassName());

		this.config = new ProxyConfig();
		this.config.setExposeProxy(enableMetrics.getBoolean("exposeProxy"));
		this.config.setProxyTargetClass(enableMetrics.getBoolean("proxyTargetClass"));

		this.scope = enableMetrics.getString("scope");
		if (!StringUtils.hasText(this.scope)) {
			this.scope = null;
		}
	}

	@Bean
	@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
	public ExceptionMeteredAnnotationBeanPostProcessor exceptionMeteredAnnotationBeanPostProcessor() {
		return new ExceptionMeteredAnnotationBeanPostProcessor(getMetricsRegistry(), config, scope);
	}

	@Bean
	@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
	public MeteredAnnotationBeanPostProcessor meteredAnnotationBeanPostProcessor() {
		return new MeteredAnnotationBeanPostProcessor(getMetricsRegistry(), config, scope);
	}

	@Bean
	@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
	public TimedAnnotationBeanPostProcessor timedAnnotationBeanPostProcessor() {
		return new TimedAnnotationBeanPostProcessor(getMetricsRegistry(), config, scope);
	}

	@Bean
	@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
	public GaugeAnnotationBeanPostProcessor gaugeAnnotationBeanPostProcessor() {
		return new GaugeAnnotationBeanPostProcessor(getMetricsRegistry(), scope);
	}

	@Bean
	@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
	public InjectedMetricAnnotationBeanPostProcessor injectedMetricAnnotationBeanPostProcessor() {
		return new InjectedMetricAnnotationBeanPostProcessor(getMetricsRegistry(), scope);
	}

	@Bean
	@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
	public HealthCheckBeanPostProcessor healthCheckBeanPostProcessor() {
		return new HealthCheckBeanPostProcessor(getHealthCheckRegistry());
	}

	protected MetricsRegistry getMetricsRegistry() {
		return Metrics.defaultRegistry();
	}

	protected HealthCheckRegistry getHealthCheckRegistry() {
		return HealthChecks.defaultRegistry();
	}

	protected void configureMetricsReporters(MetricsRegistry metricsRegistry) {}

}
