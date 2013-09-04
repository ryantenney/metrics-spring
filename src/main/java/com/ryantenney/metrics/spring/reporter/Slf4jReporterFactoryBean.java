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
package com.ryantenney.metrics.spring.reporter;

import java.util.concurrent.TimeUnit;

import org.slf4j.LoggerFactory;
import org.slf4j.MarkerFactory;

import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.Slf4jReporter;

public class Slf4jReporterFactoryBean extends AbstractScheduledReporterFactoryBean<Slf4jReporter> {

	// Required
	public static final String PERIOD = "period";

	// Optional
	public static final String DURATION_UNIT = "duration-unit";
	public static final String RATE_UNIT = "rate-unit";
	public static final String FILTER_PATTERN = "filter";
	public static final String FILTER_REF = "filter-ref";
	public static final String MARKER = "marker";
	public static final String LOGGER = "logger";

	@Override
	public Class<Slf4jReporter> getObjectType() {
		return Slf4jReporter.class;
	}

	@Override
	protected Slf4jReporter createInstance() {
		final Slf4jReporter.Builder reporter = Slf4jReporter.forRegistry(getMetricRegistry());

		if (hasProperty(DURATION_UNIT)) {
			reporter.convertDurationsTo(getProperty(DURATION_UNIT, TimeUnit.class));
		}

		if (hasProperty(RATE_UNIT)) {
			reporter.convertRatesTo(getProperty(RATE_UNIT, TimeUnit.class));
		}

		if (hasProperty(FILTER_PATTERN)) {
			reporter.filter(metricFilterPattern(getProperty(FILTER_PATTERN)));
		}
		else if (hasProperty(FILTER_REF)) {
			reporter.filter(getPropertyRef(FILTER_REF, MetricFilter.class));
		}

		if (hasProperty(MARKER)) {
			reporter.markWith(MarkerFactory.getMarker(getProperty(MARKER)));
		}

		if (hasProperty(LOGGER)) {
			reporter.outputTo(LoggerFactory.getLogger(getProperty(LOGGER)));
		}

		return reporter.build();
	}

	@Override
	protected long getPeriod() {
		return convertDurationString(getProperty(PERIOD));
	}

}
