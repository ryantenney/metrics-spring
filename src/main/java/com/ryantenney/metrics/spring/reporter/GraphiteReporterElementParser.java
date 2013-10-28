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

import static com.ryantenney.metrics.spring.reporter.GraphiteReporterFactoryBean.CHARSET;
import static com.ryantenney.metrics.spring.reporter.GraphiteReporterFactoryBean.CLOCK_REF;
import static com.ryantenney.metrics.spring.reporter.GraphiteReporterFactoryBean.DURATION_UNIT;
import static com.ryantenney.metrics.spring.reporter.GraphiteReporterFactoryBean.FILTER_PATTERN;
import static com.ryantenney.metrics.spring.reporter.GraphiteReporterFactoryBean.FILTER_REF;
import static com.ryantenney.metrics.spring.reporter.GraphiteReporterFactoryBean.HOST;
import static com.ryantenney.metrics.spring.reporter.GraphiteReporterFactoryBean.PERIOD;
import static com.ryantenney.metrics.spring.reporter.GraphiteReporterFactoryBean.PORT;
import static com.ryantenney.metrics.spring.reporter.GraphiteReporterFactoryBean.PREFIX;
import static com.ryantenney.metrics.spring.reporter.GraphiteReporterFactoryBean.RATE_UNIT;

import java.util.concurrent.TimeUnit;

public class GraphiteReporterElementParser extends AbstractReporterElementParser {

	private static final String PORT_NUMBER_REGEX = "^([1-9][0-9]{0,3}|[1-5][0-9]{4}|6[0-4][0-9]{3}|65[0-4][0-9]{2}|655[0-2][0-9]|6553[0-5])|([\\$\\#]{1}\\{.*\\})$";
	private static final String DURATION_STRING_REGEX = "^(((\\d+\\s?)|([\\$\\#]{1}\\{.*\\}))(ns|us|ms|s|m|h|d)?)|([\\$\\#]{1}\\{.*\\})$";

	@Override
	public String getType() {
		return "graphite";
	}

	@Override
	protected Class<?> getBeanClass() {
		return GraphiteReporterFactoryBean.class;
	}

	@Override
	protected void validate(ValidationContext c) {
		c.require(HOST);
		c.require(PORT, PORT_NUMBER_REGEX, "Port number is required and must be between 1-65536  or a property placeholder");
		c.require(PERIOD, DURATION_STRING_REGEX, "Period is required and must be in the form '\\d+(ns|us|ms|s|m|h|d)' or a property placeholder");

		c.optional(CHARSET);

		c.optional(PREFIX);
		c.optional(CLOCK_REF);

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
