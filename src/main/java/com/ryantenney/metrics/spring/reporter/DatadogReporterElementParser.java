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

import static com.ryantenney.metrics.spring.reporter.DatadogReporterFactoryBean.*;

public class DatadogReporterElementParser extends AbstractReporterElementParser {

	@Override
	public String getType() {
		return "datadog";
	}

	@Override
	protected Class<?> getBeanClass() {
		return DatadogReporterFactoryBean.class;
	}

	@Override
	protected void validate(ValidationContext c) {
		// Required
		c.require(TRANSPORT, "^http|udp|statsd$", "Transport must be one of: http, udp, statsd");
		c.require(PERIOD, DURATION_STRING_REGEX, "Period is required and must be in the form '\\d+(ns|us|ms|s|m|h|d)'");

		if ("http".equals(c.get(TRANSPORT))) {
			c.require(API_KEY);
		}

		// HTTP only properties
		c.optional(CONNECT_TIMEOUT, DURATION_STRING_REGEX, "Connect timeout must be in the form '\\d+(ns|us|ms|s|m|h|d)'");
		c.optional(SOCKET_TIMEOUT, DURATION_STRING_REGEX, "Socket timeout must be in the form '\\d+(ns|us|ms|s|m|h|d)'");

		// UDP only properties
		c.optional(STATSD_HOST);
		c.optional(STATSD_PORT, PORT_NUMBER_REGEX, "Port number must be an integer between 1-65536");
		c.optional(STATSD_PREFIX);

		// All
		c.optional(HOST);
		c.optional(EC2_HOST);
		c.optional(EXPANSION);
		c.optional(TAGS);
		c.optional(DYNAMIC_TAG_CALLBACK_REF);
		c.optional(METRIC_NAME_FORMATTER_REF);

		c.optional(PREFIX);
		c.optional(CLOCK_REF);

		c.optional(RATE_UNIT, TIMEUNIT_STRING_REGEX, "Rate unit must be one of the enum constants from java.util.concurrent.TimeUnit");
		c.optional(DURATION_UNIT, TIMEUNIT_STRING_REGEX, "Duration unit must be one of the enum constants from java.util.concurrent.TimeUnit");

		c.optional(FILTER_PATTERN);
		c.optional(FILTER_REF);
		if (c.has(FILTER_PATTERN) && c.has(FILTER_REF)) {
			c.reject(FILTER_REF, "Reporter element must not specify both the 'filter' and 'filter-ref' attributes");
		}

		c.rejectUnmatchedProperties();
	}

}
