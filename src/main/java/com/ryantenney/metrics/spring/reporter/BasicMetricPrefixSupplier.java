package com.ryantenney.metrics.spring.reporter;

public class BasicMetricPrefixSupplier implements MetricPrefixSupplier {

	private final String prefix;

	public BasicMetricPrefixSupplier(final String prefix) {
		this.prefix = prefix;
	}

	@Override
	public String getPrefix() {
		return prefix;
	}

}
