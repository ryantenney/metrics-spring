package com.ryantenney.metrics.spring.reporter;

import org.elasticsearch.metrics.ElasticsearchReporter;

import java.util.Map;
import java.util.concurrent.TimeUnit;


/**
 * Configuration of Metrics reporter using ES.
 * Done in @Configuration since spring xml has no good support for fluent beans.
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
            .hosts(getProperty( ElasticSearchReporterElementParser.HOSTS, String[].class ))
            .build();

        elasticsearchReporter.start(getPeriod(), TimeUnit.NANOSECONDS);

        return elasticsearchReporter;
    }

    @Override
    protected long getPeriod()
    {
        return convertDurationString(getProperty("period"));
    }
}
