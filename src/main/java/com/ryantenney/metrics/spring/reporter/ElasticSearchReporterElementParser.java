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

import static com.ryantenney.metrics.spring.reporter.AbstractReporterFactoryBean.*;

/**
 * Configuration of Metrics reporter using ES.
 *
 * @author <a href="mailto:david@davidkarlsen.com>David J. M. Karlsen</a>
 * @since 3.1.3
 */
public class ElasticSearchReporterElementParser
    extends AbstractReporterElementParser
{
    protected static final String HOSTS = "hosts";
    protected static final String ADDITIONAL_FIELDS_REF = "additionalFieldsRef";
    protected static final String PERIOD = "period";

    @Override
    public String getType() {
        return "elasticsearch";
    }

    @Override
    protected Class<?> getBeanClass() {
        return ElasticSearchReporterFactoryBean.class;
    }

    protected void validate(ValidationContext c) {
        c.require(HOSTS);
        c.require(PERIOD, DURATION_STRING_REGEX, "Period is required and must be in the form '\\d+(ns|us|ms|s|m|h|d)'");

        c.optional(FILTER_PATTERN);
        c.optional(FILTER_REF);
        if (c.has(FILTER_PATTERN) && c.has(FILTER_REF)) {
            c.reject(FILTER_REF, "Reporter element must not specify both the 'filter' and 'filter-ref' attributes");
        }
        c.optional(PREFIX);
        c.optional(PREFIX_SUPPLIER_REF);
        c.optional(ADDITIONAL_FIELDS_REF);

        c.rejectUnmatchedProperties();
    }

}
