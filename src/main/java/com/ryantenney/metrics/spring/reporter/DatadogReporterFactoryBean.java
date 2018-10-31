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

import java.io.IOException;
import java.util.EnumSet;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import org.coursera.metrics.datadog.DatadogReporter;
import org.coursera.metrics.datadog.DynamicTagsCallback;
import org.coursera.metrics.datadog.MetricNameFormatter;
import org.coursera.metrics.datadog.DatadogReporter.Expansion;
import org.coursera.metrics.datadog.transport.HttpTransport;
import org.coursera.metrics.datadog.transport.Transport;
import org.coursera.metrics.datadog.transport.UdpTransport;
import org.springframework.util.StringUtils;

import static java.util.Arrays.asList;

import com.codahale.metrics.Clock;

public class DatadogReporterFactoryBean extends AbstractScheduledReporterFactoryBean<DatadogReporter> {

	// Required
	public static final String TRANSPORT = "transport";
	public static final String PERIOD = "period";

	// HTTP Transport
	public static final String API_KEY = "api-key";
	public static final String CONNECT_TIMEOUT = "connect-timeout";
	public static final String SOCKET_TIMEOUT = "socket-timeout";

	// UDP Transport
	public static final String STATSD_HOST = "statsd-host";
	public static final String STATSD_PORT = "statsd-port";
	public static final String STATSD_PREFIX = "statsd-prefix";

	// Optional
	public static final String HOST = "host";
	public static final String EC2_HOST = "use-ec2-host";
	public static final String EXPANSION = "expansions";
	public static final String TAGS = "tags";
	public static final String DYNAMIC_TAG_CALLBACK_REF = "dynamic-tag-callback-ref";
	public static final String METRIC_NAME_FORMATTER_REF = "metric-name-formatter-ref";
	public static final String PREFIX = "prefix";
	public static final String CLOCK_REF = "clock-ref";
	public static final String DURATION_UNIT = "duration-unit";
	public static final String RATE_UNIT = "rate-unit";

	@Override
	public Class<DatadogReporter> getObjectType() {
		return DatadogReporter.class;
	}

	@SuppressWarnings("resource")
	@Override
	protected DatadogReporter createInstance() {
		final DatadogReporter.Builder reporter = DatadogReporter.forRegistry(getMetricRegistry());

		final Transport transport;
		String transportName = getProperty(TRANSPORT);
		if ("http".equalsIgnoreCase(transportName)) {
			HttpTransport.Builder builder = new HttpTransport.Builder();
			builder.withApiKey(getProperty(API_KEY));
			if (hasProperty(CONNECT_TIMEOUT)) {
				builder.withConnectTimeout(getProperty(CONNECT_TIMEOUT, Integer.class));
			}
			if (hasProperty(SOCKET_TIMEOUT)) {
				builder.withSocketTimeout(getProperty(SOCKET_TIMEOUT, Integer.class));
			}
			transport = builder.build();
		} else if ("udp".equalsIgnoreCase(transportName) || "statsd".equalsIgnoreCase(transportName)) {
			UdpTransport.Builder builder = new UdpTransport.Builder();
			if (hasProperty(STATSD_HOST)) {
				builder.withStatsdHost(getProperty(STATSD_HOST));
			}
			if (hasProperty(STATSD_PORT)) {
				builder.withPort(getProperty(STATSD_PORT, Integer.class));
			}
			if (hasProperty(STATSD_PREFIX)) {
				builder.withPrefix(getProperty(STATSD_PREFIX));
			}
			transport = builder.build();
		} else {
			throw new IllegalArgumentException("Invalid Datadog Transport: " + transportName);
		}
		reporter.withTransport(transport);

		if (hasProperty(TAGS)) {
			reporter.withTags(asList(StringUtils.tokenizeToStringArray(getProperty(TAGS), ",", true, true)));
		}

		if (StringUtils.hasText(getProperty(HOST))) {
			reporter.withHost(getProperty(HOST));
		}
		else if ("true".equalsIgnoreCase(getProperty(EC2_HOST))) {
			try {
				reporter.withEC2Host();
			}
			catch (IOException e) {
				throw new IllegalStateException("DatadogReporter.Builder.withEC2Host threw an exception", e);
			}
		}

		if (hasProperty(EXPANSION)) {
			String configString = getProperty(EXPANSION).trim().toUpperCase(Locale.ENGLISH);
			final EnumSet<Expansion> expansions;
			if ("ALL".equals(configString)) {
				expansions = Expansion.ALL;
			}
			else {
				expansions = EnumSet.noneOf(Expansion.class);
				for (String expandedMetricStr : StringUtils.tokenizeToStringArray(configString, ",", true, true)) {
					expansions.add(Expansion.valueOf(expandedMetricStr.replace(' ', '_')));
				}
			}
			reporter.withExpansions(expansions);
		}

		if (hasProperty(DYNAMIC_TAG_CALLBACK_REF)) {
			reporter.withDynamicTagCallback(getPropertyRef(DYNAMIC_TAG_CALLBACK_REF, DynamicTagsCallback.class));
		}

		if (hasProperty(METRIC_NAME_FORMATTER_REF)) {
			reporter.withMetricNameFormatter(getPropertyRef(METRIC_NAME_FORMATTER_REF, MetricNameFormatter.class));
		}

		if (hasProperty(PREFIX)) {
			reporter.withPrefix(getProperty(PREFIX));
		}

		if (hasProperty(DURATION_UNIT)) {
			reporter.convertDurationsTo(getProperty(DURATION_UNIT, TimeUnit.class));
		}

		if (hasProperty(RATE_UNIT)) {
			reporter.convertRatesTo(getProperty(RATE_UNIT, TimeUnit.class));
		}

		if (hasProperty(CLOCK_REF)) {
			reporter.withClock(getPropertyRef(CLOCK_REF, Clock.class));
		}

		reporter.filter(getMetricFilter());

		return reporter.build();
	}

	@Override
	protected long getPeriod() {
		return convertDurationString(getProperty(PERIOD));
	}

}
