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

import static com.ryantenney.metrics.spring.reporter.LibratoReporterFactoryBean.*;

public class LibratoReporterElementParser extends AbstractReporterElementParser {

	@Override
	public String getType() {
		return "librato";
	}

	@Override
	protected Class<?> getBeanClass() {
		return LibratoReporterFactoryBean.class;
	}

	@Override
	protected void validate(ValidationContext c) {
		c.require(USERNAME);
		c.require(TOKEN);
		c.require(PERIOD, DURATION_STRING_REGEX, "Period is required and must be in the form '\\d+(ns|us|ms|s|m|h|d)'");

		c.optional(SOURCE);
		c.optional(SOURCE_SUPPLIER_REF);
		if (!c.has(SOURCE) && !c.has(SOURCE_SUPPLIER_REF)) {
			c.require(SOURCE);
		}
		else if (c.has(SOURCE) && c.has(SOURCE_SUPPLIER_REF)) {
			c.reject(SOURCE_SUPPLIER_REF, "Reporter element must not specify both the 'source' and 'source-supplier-ref' attributes");
		}

		c.optional(TIMEOUT, DURATION_STRING_REGEX, "Timeout must be in the form '\\d+(ns|us|ms|s|m|h|d)'");
		c.optional(NAME);
		c.optional(SANITIZER_REF);
		c.optional(PREFIX_DELIMITER);
		c.optional(CLOCK_REF);
		c.optional(SOURCE_REGEX);

		c.optional(HTTP_POSTER_REF);
		c.optional(HTTP_CLIENT_CONFIG_REF);

		c.optional(DELETE_IDLE_STATS);
		c.optional(OMIT_COMPLEX_GAUGES);

		c.optional(EXPANSION_CONFIG);
		c.optional(EXPANSION_CONFIG_REF);
		if (c.has(EXPANSION_CONFIG) && c.has(EXPANSION_CONFIG_REF)) {
			c.reject(EXPANSION_CONFIG, "Librato Reporter element must not specify both the 'expansion-config' and 'expansion-config-ref' attributes");
		}

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
