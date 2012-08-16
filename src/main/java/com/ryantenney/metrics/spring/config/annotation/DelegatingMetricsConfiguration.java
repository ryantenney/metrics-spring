package com.ryantenney.metrics.spring.config.annotation;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.yammer.metrics.core.HealthCheckRegistry;
import com.yammer.metrics.core.MetricsRegistry;

/**
 * This is the class imported by {@link EnableMetrics @EnableMetrics}.
 * 
 * @see MetricsConfigurer
 * @see MetricsConfigurationSupport
 * @author Ryan Tenney
 * @since 2.2
 */
@Configuration
public class DelegatingMetricsConfiguration extends MetricsConfigurationSupport implements MetricsConfigurer {

	private MetricsConfigurerComposite delegates = new MetricsConfigurerComposite();

	private MetricsRegistry metricsRegistry;
	private HealthCheckRegistry healthCheckRegistry;

	@Autowired(required = false)
	public void setMetricsConfigurers(final List<MetricsConfigurer> configurers) {
		if (configurers != null) {
			this.delegates.addMetricsConfigurers(configurers);
		}
	}

	@Override
	public void configureMetricsReporters(final MetricsRegistry metricsRegistry) {
		this.delegates.configureMetricsReporters(metricsRegistry);
	}

	@Bean
	@Override
	public MetricsRegistry getMetricsRegistry() {
		if (this.metricsRegistry == null) {
			this.metricsRegistry = this.delegates.getMetricsRegistry();
			this.configureMetricsReporters(this.metricsRegistry);
		}
		return this.metricsRegistry;
	}

	@Bean
	@Override
	public HealthCheckRegistry getHealthCheckRegistry() {
		if (this.healthCheckRegistry == null) {
			this.healthCheckRegistry = this.delegates.getHealthCheckRegistry();
		}
		return this.healthCheckRegistry;
	}

}
