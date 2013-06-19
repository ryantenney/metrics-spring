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
package com.ryantenney.metrics.spring.reporter;

import static com.ryantenney.metrics.spring.reporter.ConsoleReporterFactoryBean.*;

import java.util.concurrent.TimeUnit;

public class ConsoleReporterElementParser extends AbstractReporterElementParser {

	private static final String DURATION_STRING_REGEX = "^(\\d+)\\s?(ns|us|ms|s|m|h|d)?$";
	private static final String LOCALE_STRING_REGEX = "^[a-z]{2}(_[A-Z]{2})?$";

	@Override
	public String getType() {
		return "console";
	}

	@Override
	protected Class<?> getBeanClass() {
		return ConsoleReporterFactoryBean.class;
	}

	@Override
	protected void validate(ValidationContext c) {
		c.require(PERIOD, DURATION_STRING_REGEX, "Period is required and must be in the form '\\d+(ns|us|ms|s|m|h|d)'");

		c.optional(CLOCK_REF);
		c.optional(OUTPUT_REF);

		c.optional(LOCALE, LOCALE_STRING_REGEX, "Locale must be in the proper format");
		c.optional(TIMEZONE); // Difficult to validate, if invalid will fall back to GMT

		if (c.optional(RATE_UNIT)) {
			TimeUnit.valueOf(c.get(RATE_UNIT));
		}
		if (c.optional(DURATION_UNIT)) {
			TimeUnit.valueOf(c.get(DURATION_UNIT));
		}

		c.optional(FILTER_PATTERN);
		c.optional(FILTER_REF);
		if (c.has(FILTER_PATTERN) && c.has(FILTER_REF)) {
			c.reject(FILTER_REF, "Reporter element not specify both the 'filter' and 'filter-ref' attributes");
		}

		c.rejectUnmatchedProperties();
	}

}
