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

		if (c.has(RATE_UNIT)) {
			TimeUnit.valueOf(c.get(RATE_UNIT));
		}
		if (c.has(DURATION_UNIT)) {
			TimeUnit.valueOf(c.get(DURATION_UNIT));
		}
	}

}
