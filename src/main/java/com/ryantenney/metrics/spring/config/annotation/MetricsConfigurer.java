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

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheckRegistry;

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
	 * @param metricRegistry
	 */
	void configureReporters(MetricRegistry metricRegistry);

	/**
	 * Override this method to provide a custom {@code MetricRegistry}.
	 * @return
	 */
	MetricRegistry getMetricRegistry();

	/**
	 * Override this method to provide a custom {@code HealthCheckRegistry}.
	 * @return
	 */
	HealthCheckRegistry getHealthCheckRegistry();

}
