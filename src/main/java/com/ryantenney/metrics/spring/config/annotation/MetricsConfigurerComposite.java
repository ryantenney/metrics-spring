package com.ryantenney.metrics.spring.config.annotation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.yammer.metrics.HealthChecks;
import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.HealthCheckRegistry;
import com.yammer.metrics.core.MetricsRegistry;

/**
 * A {@link MetricsConfigurer} implementation that delegates to other {@link MetricsConfigurer} instances.
 * 
 * @author Ryan Tenney
 * @since 2.2
 */
public class MetricsConfigurerComposite implements MetricsConfigurer {

	private final List<MetricsConfigurer> configurers = new ArrayList<MetricsConfigurer>();

	public void addMetricsConfigurers(final Collection<MetricsConfigurer> configurers) {
		if (configurers != null) {
			this.configurers.addAll(configurers);
		}
	}

	@Override
	public void configureMetricsReporters(final MetricsRegistry metricsRegistry) {
		for (MetricsConfigurer configurer : this.configurers) {
			configurer.configureMetricsReporters(metricsRegistry);
		}
	}

	@Override
	public MetricsRegistry getMetricsRegistry() {
		List<MetricsRegistry> candidates = new ArrayList<MetricsRegistry>();
		for (MetricsConfigurer configurer : this.configurers) {
			MetricsRegistry healthCheckRegistry = configurer.getMetricsRegistry();
			if (healthCheckRegistry != null) {
				candidates.add(healthCheckRegistry);
			}
		}
		return selectSingleInstance(candidates, MetricsRegistry.class, Metrics.defaultRegistry());
	}

	@Override
	public HealthCheckRegistry getHealthCheckRegistry() {
		List<HealthCheckRegistry> candidates = new ArrayList<HealthCheckRegistry>();
		for (MetricsConfigurer configurer : this.configurers) {
			HealthCheckRegistry healthCheckRegistry = configurer.getHealthCheckRegistry();
			if (healthCheckRegistry != null) {
				candidates.add(healthCheckRegistry);
			}
		}
		return selectSingleInstance(candidates, HealthCheckRegistry.class, HealthChecks.defaultRegistry());
	}

	private <T> T selectSingleInstance(final List<T> instances, final Class<T> instanceType, final T defaultInstance) {
		if (instances.size() > 1) {
			throw new IllegalStateException("Only one [" + instanceType + "] was expected but multiple instances were provided: " + instances);
		} else if (instances.size() == 1) {
			return instances.get(0);
		} else {
			return defaultInstance;
		}
	}

}
