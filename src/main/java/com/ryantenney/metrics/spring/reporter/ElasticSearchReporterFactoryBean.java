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

import org.elasticsearch.metrics.ElasticsearchReporter;

import java.util.Map;
import java.util.concurrent.TimeUnit;


/**
 * Configuration of Metrics reporter using ES.
 *
 * @author <a href="mailto:david@davidkarlsen.com>David J. M. Karlsen</a>
 * @since 3.1.3
 */
public class ElasticSearchReporterFactoryBean
    extends AbstractScheduledReporterFactoryBean<ElasticsearchReporter>
{

    @Override
    public Class<? extends ElasticsearchReporter> getObjectType()
    {
        return ElasticsearchReporter.class;
    }

    @Override
    protected ElasticsearchReporter createInstance()
        throws Exception
    {
        ElasticsearchReporter elasticsearchReporter = ElasticsearchReporter.forRegistry(getMetricRegistry())
            .additionalFields(getPropertyRef(ElasticSearchReporterElementParser.ADDITIONAL_FIELDS_REF, Map.class))
            .prefixedWith(getPrefix())
            .filter(getMetricFilter())
            .hosts(getProperty(ElasticSearchReporterElementParser.HOSTS, String[].class))
            .build();

        return elasticsearchReporter;
    }

    @Override
    protected long getPeriod()
    {
        return convertDurationString(getProperty("period"));
    }
}
