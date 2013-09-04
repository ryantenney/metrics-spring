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

import com.codahale.metrics.MetricFilter;

public class FakeReporterFactoryBean extends AbstractScheduledReporterFactoryBean<FakeReporter> {

	// Required
	public static final String PERIOD = "period";
	public static final String DURATION_UNIT = "duration-unit";
	public static final String RATE_UNIT = "rate-unit";

	// Optional
	public static final String FILTER_PATTERN = "filter";
	public static final String FILTER_REF = "filter-ref";

	@Override
	public Class<FakeReporter> getObjectType() {
		return FakeReporter.class;
	}

	@Override
	protected FakeReporter createInstance() {
		final MetricFilter filter;
		if (hasProperty(FILTER_PATTERN)) {
			filter = metricFilterPattern(getProperty(FILTER_PATTERN));
		}
		else if (hasProperty(FILTER_REF)) {
			filter = getPropertyRef(FILTER_REF, MetricFilter.class);
		}
		else {
			filter = MetricFilter.ALL;
		}

		final TimeUnit durationUnit = getProperty(DURATION_UNIT, TimeUnit.class);
		final TimeUnit rateUnit = getProperty(RATE_UNIT, TimeUnit.class);

		return new FakeReporter(getMetricRegistry(), filter, rateUnit, durationUnit);
	}

	protected long getPeriod() {
		return convertDurationString(getProperty(PERIOD));
	}

}
