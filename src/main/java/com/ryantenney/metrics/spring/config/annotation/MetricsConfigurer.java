package com.ryantenney.metrics.spring.config.annotation;

import com.yammer.metrics.core.HealthCheckRegistry;
import com.yammer.metrics.core.MetricsRegistry;

/**
 * Defines callback methods to customize the Java-based configuration
 * for Spring Metrics enabled via {@link EnableMetrics @EnableMetrics}.
 * 
 * @see EnableMetrics
 * @author Ryan Tenney
 * @since 3.0
 */
public interface MetricsConfigurer {

	/**
	 * Configure reporters.
	 * @param metricsRegistry
	 */
	public void configureMetricsReporters(MetricsRegistry metricsRegistry);

	/**
	 * Override this method to provide a custom {@code MetricsRegistry}.
	 * @return
	 */
	public MetricsRegistry getMetricsRegistry();

	/**
	 * Override this method to provide a custom {@code HealthCheckRegistry}.
	 * @return
	 */
	public HealthCheckRegistry getHealthCheckRegistry();

}
