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

import static com.ryantenney.metrics.spring.reporter.FakeReporterFactoryBean.*;

import java.util.concurrent.TimeUnit;

public class FakeReporterElementParser extends AbstractReporterElementParser {

	@Override
	public String getType() {
		return "fake";
	}

	@Override
	protected Class<?> getBeanClass() {
		return FakeReporterFactoryBean.class;
	}

	@Override
	protected void validate(ValidationContext c) {
		c.require(PERIOD);
		TimeUnit.valueOf(c.require(DURATION_UNIT));
		TimeUnit.valueOf(c.require(RATE_UNIT));
		c.optional(FILTER_PATTERN);
		c.optional(FILTER_REF);
		c.rejectUnmatchedProperties();
	}

}
