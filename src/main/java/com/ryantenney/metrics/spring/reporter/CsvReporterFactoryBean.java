/**
 * Copyright © 2012 Ryan W Tenney (ryan@10e.us)
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
package com.ryantenney.metrics.spring.reporter;

import java.io.File;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import com.codahale.metrics.Clock;
import com.codahale.metrics.CsvReporter;

public class CsvReporterFactoryBean extends AbstractScheduledReporterFactoryBean<CsvReporter> {

	// Required
	public static final String PERIOD = "period";

	// Optional
	public static final String CLOCK_REF = "clock-ref";
	public static final String DIRECTORY = "directory";
	public static final String LOCALE = "locale";
	public static final String DURATION_UNIT = "duration-unit";
	public static final String RATE_UNIT = "rate-unit";

	@Override
	public Class<CsvReporter> getObjectType() {
		return CsvReporter.class;
	}

	@Override
	protected CsvReporter createInstance() {
		final CsvReporter.Builder reporter = CsvReporter.forRegistry(getMetricRegistry());

		if (hasProperty(DURATION_UNIT)) {
			reporter.convertDurationsTo(getProperty(DURATION_UNIT, TimeUnit.class));
		}

		if (hasProperty(RATE_UNIT)) {
			reporter.convertRatesTo(getProperty(RATE_UNIT, TimeUnit.class));
		}

		reporter.filter(getMetricFilter());

		if (hasProperty(CLOCK_REF)) {
			reporter.withClock(getPropertyRef(CLOCK_REF, Clock.class));
		}

		if (hasProperty(LOCALE)) {
			reporter.formatFor(parseLocale(getProperty(LOCALE)));
		}

		File dir = new File(getProperty(DIRECTORY));
		if (!dir.mkdirs() && !dir.isDirectory()) {
			throw new IllegalArgumentException("Directory doesn't exist or couldn't be created");
		}

		return reporter.build(dir);
	}

	@Override
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
