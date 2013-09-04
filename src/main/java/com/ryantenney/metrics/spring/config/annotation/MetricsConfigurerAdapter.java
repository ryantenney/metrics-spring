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

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheckRegistry;

/**
 * An implementation of {@link MetricsConfigurer} with empty methods allowing
 * sub-classes to override only the methods they're interested in.
 *
 * @see EnableMetrics
 * @see MetricsConfigurer
 * @author Ryan Tenney
 * @since 3.0
 */
public abstract class MetricsConfigurerAdapter implements MetricsConfigurer {

	/**
	 * {@inheritDoc}
	 * <p>This implementation is empty.
	 */
	@Override
	public void configureReporters(MetricRegistry metricRegistry) {
	}

	/**
	 * {@inheritDoc}
	 * <p>This implementation returns {@code null}.
	 */
	@Override
	public MetricRegistry getMetricRegistry() {
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
