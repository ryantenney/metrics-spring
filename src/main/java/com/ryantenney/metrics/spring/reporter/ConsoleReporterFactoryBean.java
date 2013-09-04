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

import java.io.PrintStream;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import com.codahale.metrics.Clock;
import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.MetricFilter;

public class ConsoleReporterFactoryBean extends AbstractScheduledReporterFactoryBean<ConsoleReporter> {

	// Required
	public static final String PERIOD = "period";

	// Optional
	public static final String CLOCK_REF = "clock-ref";
	public static final String OUTPUT_REF = "output-ref";
	public static final String LOCALE = "locale";
	public static final String TIMEZONE = "timezone";
	public static final String DURATION_UNIT = "duration-unit";
	public static final String RATE_UNIT = "rate-unit";
	public static final String FILTER_PATTERN = "filter";
	public static final String FILTER_REF = "filter-ref";

	@Override
	public Class<ConsoleReporter> getObjectType() {
		return ConsoleReporter.class;
	}

	@Override
	protected ConsoleReporter createInstance() {
		final ConsoleReporter.Builder reporter = ConsoleReporter.forRegistry(getMetricRegistry());

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

		if (hasProperty(CLOCK_REF)) {
			reporter.withClock(getPropertyRef(CLOCK_REF, Clock.class));
		}

		if (hasProperty(OUTPUT_REF)) {
			reporter.outputTo(getPropertyRef(OUTPUT_REF, PrintStream.class));
		}

		if (hasProperty(LOCALE)) {
			reporter.formattedFor(parseLocale(getProperty(LOCALE)));
		}

		if (hasProperty(TIMEZONE)) {
			reporter.formattedFor(TimeZone.getTimeZone(getProperty(TIMEZONE)));
		}

		return reporter.build();
	}

	protected long getPeriod() {
		return convertDurationString(getProperty(PERIOD));
	}

	protected Locale parseLocale(String localeString) {
		final int underscore = localeString.indexOf('_');
		if (underscore == -1) {
			return new Locale(localeString);
		}
		else {
			return new Locale(localeString.substring(0, underscore), localeString.substring(underscore + 1));
		}
	}

}
