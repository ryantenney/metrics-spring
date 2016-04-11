package com.ryantenney.metrics.spring;

import java.util.regex.Pattern;

import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricFilter;

public class RegexMetricFilter implements MetricFilter {
    private final Pattern filter;

    public RegexMetricFilter(String pattern) {
        this(Pattern.compile(pattern));
    }

    private RegexMetricFilter(Pattern filter) {
        this.filter = filter;
    }

    @Override
    public boolean matches(String name, Metric metric) {
        return filter.matcher(name).matches();
    }

    @Override
    public String toString() {
        return "[MetricFilter regex=" + filter.pattern() + "]";
    }
}
