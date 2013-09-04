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

import info.ganglia.gmetric4j.gmetric.GMetric;
import info.ganglia.gmetric4j.gmetric.GMetric.UDPAddressingMode;

import java.util.concurrent.TimeUnit;

import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.ganglia.GangliaReporter;

public class GangliaReporterFactoryBean extends AbstractScheduledReporterFactoryBean<GangliaReporter> {

	// Required
	public static final String GROUP = "group";
	public static final String PORT = "port";
	public static final String UDP_MODE = "udp-mode";
	public static final String TTL = "ttl";
	public static final String PERIOD = "period";

	// Optional
	public static final String PREFIX = "prefix";
	public static final String DURATION_UNIT = "duration-unit";
	public static final String RATE_UNIT = "rate-unit";
	public static final String FILTER_PATTERN = "filter";
	public static final String FILTER_REF = "filter-ref";
	public static final String PROTOCOL = "protocol";
	public static final String UUID = "uuid";
	public static final String SPOOF = "spoof";
	public static final String DMAX = "dmax";
	public static final String TMAX = "tmax";

	@Override
	public Class<GangliaReporter> getObjectType() {
		return GangliaReporter.class;
	}

	@Override
	protected GangliaReporter createInstance() throws Exception {
		final GangliaReporter.Builder reporter = GangliaReporter.forRegistry(getMetricRegistry());

		if (hasProperty(PREFIX)) {
			reporter.prefixedWith(getProperty(PREFIX));
		}

		if (hasProperty(DURATION_UNIT)) {
			reporter.convertDurationsTo(getProperty(DURATION_UNIT, TimeUnit.class));
		}

		if (hasProperty(RATE_UNIT)) {
			reporter.convertRatesTo(getProperty(RATE_UNIT, TimeUnit.class));
		}

		if (hasProperty(FILTER_PATTERN)) {
			reporter.filter(metricFilterPattern(getProperty(FILTER_PATTERN)));
		}
		else if (hasProperty(FILTER_REF)) {
			reporter.filter(getPropertyRef(FILTER_REF, MetricFilter.class));
		}

		if (hasProperty(DMAX)) {
			reporter.withDMax(getProperty(DMAX, Integer.TYPE));
		}

		if (hasProperty(TMAX)) {
			reporter.withTMax(getProperty(TMAX, Integer.TYPE));
		}

		final GMetric gMetric = new GMetric(
			getProperty(GROUP),
			getProperty(PORT, Integer.TYPE),
			getProperty(UDP_MODE, UDPAddressingMode.class),
			getProperty(TTL, Integer.TYPE),
			!hasProperty(PROTOCOL) || getProperty(PROTOCOL).contains("3.1"),
			hasProperty(UUID) ? java.util.UUID.fromString(getProperty(UUID)) : null,
			getProperty(SPOOF)
		);

		return reporter.build(gMetric);
	}

	protected long getPeriod() {
		return convertDurationString(getProperty(PERIOD));
	}

}
