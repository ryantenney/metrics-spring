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


public class NewRelicReporterElementParser extends AbstractReporterElementParser {

    @Override
    public String getType() {
        return "newrelic";
    }

    @Override
    protected void validate(ValidationContext context) {
        context.require(NewRelicReporterFactoryBean.PERIOD, DURATION_STRING_REGEX, "Period is required and must be in the form '\\d+(ns|us|ms|s|m|h|d)'");

        context.optional(NewRelicReporterFactoryBean.NAME);
        context.optional(NewRelicReporterFactoryBean.RATE_UNIT, TIMEUNIT_STRING_REGEX, "Rate unit must be one of the enum constants from java.util.concurrent.TimeUnit");
        context.optional(NewRelicReporterFactoryBean.DURATION_UNIT, TIMEUNIT_STRING_REGEX, "Duration unit must be one of the enum constants from java.util.concurrent.TimeUnit");
        context.optional(NewRelicReporterFactoryBean.ATTRIBUTE_FILTER);
        context.optional(NewRelicReporterFactoryBean.PREFIX);

        context.rejectUnmatchedProperties();
    }

    protected Class<?> getBeanClass() {
        return NewRelicReporterFactoryBean.class;
    }


}
