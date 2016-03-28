package com.ryantenney.metrics.spring.reporter;


import static com.ryantenney.metrics.spring.reporter.AbstractReporterFactoryBean.*;

/**
 * Created by david on 28/03/16.
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
