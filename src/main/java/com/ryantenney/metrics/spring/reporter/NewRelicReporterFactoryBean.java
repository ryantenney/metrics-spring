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

import com.codahale.metrics.MetricFilter;
import com.palominolabs.metrics.newrelic.AllEnabledMetricAttributeFilter;
import com.palominolabs.metrics.newrelic.MetricAttributeFilter;
import com.palominolabs.metrics.newrelic.NewRelicReporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class NewRelicReporterFactoryBean extends AbstractScheduledReporterFactoryBean<NewRelicReporter> {

    private static final Logger LOG = LoggerFactory.getLogger(NewRelicReporterFactoryBean.class);
    private static final String EMPTY_STRING = "";

    protected static final String PREFIX = "prefix";
    protected static final String PERIOD = "period";
    protected static final String DURATION_UNIT = "duration-unit";
    protected static final String RATE_UNIT = "rate-unit";
    protected static final String NAME = "name";
    protected static final String ATTRIBUTE_FILTER = "attribute-filter-ref";

    @Override
    protected long getPeriod() {
        return convertDurationString(getProperty(PERIOD));
    }

    @Override
    public Class<NewRelicReporter> getObjectType() {
        return NewRelicReporter.class;
    }

    @Override
    protected NewRelicReporter createInstance() throws Exception {

        String prefix = this.getProperty(PREFIX, String.class, EMPTY_STRING);
        MetricFilter metricFilter = getMetricFilter();
        TimeUnit duration = this.getProperty(DURATION_UNIT, TimeUnit.class, TimeUnit.MILLISECONDS);
        TimeUnit rateUnit = this.getProperty(RATE_UNIT, TimeUnit.class, TimeUnit.SECONDS);
        String name = this.getProperty(NAME, String.class, "NewRelic reporter");
        MetricAttributeFilter attributeFilter = this.hasProperty(ATTRIBUTE_FILTER) ?
                this.getPropertyRef(ATTRIBUTE_FILTER, MetricAttributeFilter.class) :
                new AllEnabledMetricAttributeFilter();

        LOG.debug("Creating instance of NewRelicReporter with name '{}', prefix '{}', rate unit '{}', duration '{}', filter '{}' and attribute filter '{}'",
                name, prefix, rateUnit, duration, metricFilter, attributeFilter.getClass().getSimpleName());

        return NewRelicReporter.forRegistry(this.getMetricRegistry())
                .name(name)
                .filter(metricFilter)
                .attributeFilter(attributeFilter)
                .rateUnit(rateUnit)
                .durationUnit(duration)
                .metricNamePrefix(prefix)
                .build();
    }

}
