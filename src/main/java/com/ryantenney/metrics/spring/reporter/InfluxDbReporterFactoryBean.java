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

import metrics_influxdb.Influxdb;
import metrics_influxdb.InfluxdbReporter;

import com.codahale.metrics.Clock;

public class InfluxDbReporterFactoryBean extends AbstractScheduledReporterFactoryBean<InfluxdbReporter> {

	// Required
	public static final String HOST = "host";
	public static final String PORT = "port";
	public static final String DATABASE = "database";
	public static final String USERNAME = "username";
	public static final String PASSWORD = "password";
	public static final String TIME_PRECISION = "time-precision";
	public static final String PERIOD = "period";

	// Optional
	public static final String PREFIX = "prefix";
	public static final String CLOCK_REF = "clock-ref";
	public static final String DURATION_UNIT = "duration-unit";
	public static final String RATE_UNIT = "rate-unit";

	@Override
	public Class<InfluxdbReporter> getObjectType() {
		return InfluxdbReporter.class;
	}

	@Override
	protected InfluxdbReporter createInstance() throws Exception {
		final InfluxdbReporter.Builder reporter = InfluxdbReporter.forRegistry(getMetricRegistry());

		if (hasProperty(PREFIX)) {
			reporter.prefixedWith(getProperty(PREFIX));
		}

		if (hasProperty(CLOCK_REF)) {
			reporter.withClock(getPropertyRef(CLOCK_REF, Clock.class));
		}

		if (hasProperty(DURATION_UNIT)) {
			reporter.convertDurationsTo(getProperty(DURATION_UNIT, TimeUnit.class));
		}

		if (hasProperty(RATE_UNIT)) {
			reporter.convertRatesTo(getProperty(RATE_UNIT, TimeUnit.class));
		}

		reporter.filter(getMetricFilter());

		Influxdb influxdb = new Influxdb(
				getProperty(HOST),
				getProperty(PORT, Integer.TYPE),
				getProperty(DATABASE),
				getProperty(USERNAME),
				getProperty(PASSWORD),
				getProperty(TIME_PRECISION, TimeUnit.class));

		return reporter.build(influxdb);
	}

	@Override
	protected long getPeriod() {
		return convertDurationString(getProperty(PERIOD));
	}

}
