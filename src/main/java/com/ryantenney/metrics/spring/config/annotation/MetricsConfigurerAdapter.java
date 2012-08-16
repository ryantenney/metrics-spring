package com.ryantenney.metrics.spring.config.annotation;

import com.yammer.metrics.core.HealthCheckRegistry;
import com.yammer.metrics.core.MetricsRegistry;

/**
 * An implementation of {@link MetricsConfigurer} with empty methods allowing
 * sub-classes to override only the methods they're interested in.
 * 
 * @see EnableMetrics
 * @see MetricsConfigurer
 * @author Ryan Tenney
 * @since 2.2
 */
public abstract class MetricsConfigurerAdapter implements MetricsConfigurer {

	/**
	 * {@inheritDoc}
	 * <p>This implementation is empty.
	 */
	@Override
	public void configureMetricsReporters(MetricsRegistry metricsRegistry) {}

	/**
	 * {@inheritDoc}
	 * <p>This implementation returns {@code null}.
	 */
	@Override
	public MetricsRegistry getMetricsRegistry() {
		return null;
	}

	/**
	 * {@inheritDoc}
	 * <p>This implementation returns {@code null}.
	 */
	@Override
	public HealthCheckRegistry getHealthCheckRegistry() {
		return null;
	}

}
