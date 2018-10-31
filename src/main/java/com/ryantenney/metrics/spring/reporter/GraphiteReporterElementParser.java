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

import static com.ryantenney.metrics.spring.reporter.GraphiteReporterFactoryBean.*;

public class GraphiteReporterElementParser extends AbstractReporterElementParser {

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
		c.require(PERIOD, DURATION_STRING_REGEX, "Period is required and must be in the form '\\d+(ns|us|ms|s|m|h|d)'");

		c.optional(TRANSPORT);

		// TCP, UDP, Pickle
		if (!c.has(TRANSPORT) || c.get(TRANSPORT).matches("^tcp|udp|pickle$")) {
			c.require(HOST);
			c.require(PORT, PORT_NUMBER_REGEX, "Port number is required and must be between 1-65536");

			c.optional(CHARSET);

			if ("pickle".equals(c.get(TRANSPORT))) {
				c.optional(BATCH_SIZE);
			}
		}
		else if (c.get(TRANSPORT).equals("rabbitmq")) {
			c.require(CONNECTION_FACTORY_REF);
			c.require(EXCHANGE);
		}

		c.optional(CLOCK_REF);

		c.optional(RATE_UNIT, TIMEUNIT_STRING_REGEX, "Rate unit must be one of the enum constants from java.util.concurrent.TimeUnit");
		c.optional(DURATION_UNIT, TIMEUNIT_STRING_REGEX, "Duration unit must be one of the enum constants from java.util.concurrent.TimeUnit");

		c.optional(PREFIX);
		c.optional(PREFIX_SUPPLIER_REF);
		if (c.has(PREFIX) && c.has(PREFIX_SUPPLIER_REF)) {
			c.reject(PREFIX_SUPPLIER_REF, "Reporter element must not specify both the 'prefix' and 'prefix-supplier-ref' attributes");
		}

		c.optional(FILTER_PATTERN);
		c.optional(FILTER_REF);
		if (c.has(FILTER_PATTERN) && c.has(FILTER_REF)) {
			c.reject(FILTER_REF, "Reporter element must not specify both the 'filter' and 'filter-ref' attributes");
		}

		c.rejectUnmatchedProperties();
	}

}
