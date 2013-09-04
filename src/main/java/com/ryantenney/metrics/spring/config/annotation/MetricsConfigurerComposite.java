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
package com.ryantenney.metrics.spring.config.annotation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheckRegistry;

/**
 * A {@link MetricsConfigurer} implementation that delegates to other {@link MetricsConfigurer} instances.
 *
 * @author Ryan Tenney
 * @since 3.0
 */
public class MetricsConfigurerComposite implements MetricsConfigurer {

	private final List<MetricsConfigurer> configurers = new ArrayList<MetricsConfigurer>();

	public void addMetricsConfigurers(final Collection<MetricsConfigurer> configurers) {
		if (configurers != null) {
			this.configurers.addAll(configurers);
		}
	}

	@Override
	public void configureReporters(final MetricRegistry metricRegistry) {
		for (MetricsConfigurer configurer : this.configurers) {
			configurer.configureReporters(metricRegistry);
		}
	}

	@Override
	public MetricRegistry getMetricRegistry() {
		final List<MetricRegistry> candidates = new ArrayList<MetricRegistry>();
		for (MetricsConfigurer configurer : this.configurers) {
			final MetricRegistry metricRegistry = configurer.getMetricRegistry();
			if (metricRegistry != null) {
				candidates.add(metricRegistry);
			}
		}
		MetricRegistry instance = selectSingleInstance(candidates, MetricRegistry.class);
		if (instance == null) {
			instance = new MetricRegistry();
		}
		return instance;
	}

	@Override
	public HealthCheckRegistry getHealthCheckRegistry() {
		final List<HealthCheckRegistry> candidates = new ArrayList<HealthCheckRegistry>();
		for (MetricsConfigurer configurer : this.configurers) {
			final HealthCheckRegistry healthCheckRegistry = configurer.getHealthCheckRegistry();
			if (healthCheckRegistry != null) {
				candidates.add(healthCheckRegistry);
			}
		}
		HealthCheckRegistry instance = selectSingleInstance(candidates, HealthCheckRegistry.class);
		if (instance == null) {
			instance = new HealthCheckRegistry();
		}
		return instance;
	}

	private <T> T selectSingleInstance(final List<T> instances, final Class<T> instanceType) {
		if (instances.size() > 1) {
			throw new IllegalStateException("Only one [" + instanceType +
					"] was expected but multiple instances were provided: " + instances);
		}
		else if (instances.size() == 1) {
			return instances.get(0);
		}
		else {
			return null;
		}
	}

}
