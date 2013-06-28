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

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheckRegistry;

/**
 * This is the class imported by {@link EnableMetrics @EnableMetrics}.
 *
 * @see MetricsConfigurer
 * @see MetricsConfigurationSupport
 * @author Ryan Tenney
 * @since 3.0
 */
@Configuration
public class DelegatingMetricsConfiguration extends MetricsConfigurationSupport implements MetricsConfigurer {

	private MetricsConfigurerComposite delegates = new MetricsConfigurerComposite();

	private MetricRegistry metricRegistry;
	private HealthCheckRegistry healthCheckRegistry;

	@Autowired(required = false)
	public void setMetricsConfigurers(final List<MetricsConfigurer> configurers) {
		if (configurers != null) {
			this.delegates.addMetricsConfigurers(configurers);
		}
	}

	@Override
	public void configureReporters(final MetricRegistry metricRegistry) {
		this.delegates.configureReporters(metricRegistry);
	}

	@Bean
	@Override
	public MetricRegistry getMetricRegistry() {
		if (this.metricRegistry == null) {
			this.metricRegistry = this.delegates.getMetricRegistry();
			this.configureReporters(this.metricRegistry);
		}
		return this.metricRegistry;
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
