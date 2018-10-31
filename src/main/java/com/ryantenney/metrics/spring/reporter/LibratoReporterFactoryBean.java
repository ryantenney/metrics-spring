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

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import org.springframework.util.StringUtils;

import com.codahale.metrics.Clock;
import com.librato.metrics.HttpPoster;
import com.librato.metrics.LibratoReporter;
import com.librato.metrics.LibratoReporter.ExpandedMetric;
import com.librato.metrics.LibratoReporter.MetricExpansionConfig;
import com.librato.metrics.Sanitizer;
import com.ning.http.client.AsyncHttpClientConfig;

public class LibratoReporterFactoryBean extends AbstractScheduledReporterFactoryBean<LibratoReporter> {

	// Required
	public static final String USERNAME = "username";
	public static final String TOKEN = "token";
	public static final String PERIOD = "period";

	public static final String SOURCE = "source";
	public static final String SOURCE_SUPPLIER_REF = "source-supplier-ref";

	// Optional
	public static final String TIMEOUT = "timeout";
	public static final String NAME = "name";
	public static final String SANITIZER_REF = "sanitizer-ref";
	public static final String EXPANSION_CONFIG = "expansion-config";
	public static final String EXPANSION_CONFIG_REF = "expansion-config-ref";
	public static final String HTTP_POSTER_REF = "http-poster-ref";
	public static final String HTTP_CLIENT_CONFIG_REF = "http-client-config-ref";
	public static final String SOURCE_REGEX = "source-regex";

	public static final String DELETE_IDLE_STATS = "delete-idle-stats";
	public static final String OMIT_COMPLEX_GAUGES = "omit-complex-gauges";
	public static final String PREFIX_DELIMITER = "prefix-delimiter";
	public static final String CLOCK_REF = "clock-ref";
	public static final String DURATION_UNIT = "duration-unit";
	public static final String RATE_UNIT = "rate-unit";

	@Override
	public Class<LibratoReporter> getObjectType() {
		return LibratoReporter.class;
	}

	@Override
	protected LibratoReporter createInstance() {
		final String username = getProperty(USERNAME);
		final String token = getProperty(TOKEN);

		final String source;
		if (hasProperty(SOURCE_SUPPLIER_REF)) {
			source = getPropertyRef(SOURCE_SUPPLIER_REF, MetricPrefixSupplier.class).getPrefix();
		}
		else {
			source = getProperty(SOURCE);
		}

		final LibratoReporter.Builder reporter = LibratoReporter.builder(getMetricRegistry(), username, token, source);

		if (hasProperty(TIMEOUT)) {
			reporter.setTimeout(convertDurationString(getProperty(TIMEOUT)), TimeUnit.NANOSECONDS);
		}

		if (hasProperty(NAME)) {
			reporter.setName(getProperty(NAME));
		}

		if (hasProperty(SANITIZER_REF)) {
			reporter.setSanitizer(getPropertyRef(SANITIZER_REF, Sanitizer.class));
		}

		if (hasProperty(EXPANSION_CONFIG)) {
			String configString = getProperty(EXPANSION_CONFIG).trim().toUpperCase(Locale.ENGLISH);
			final MetricExpansionConfig config;
			if ("ALL".equals(configString)) {
				config = MetricExpansionConfig.ALL;
			}
			else {
				Set<ExpandedMetric> set = new HashSet<ExpandedMetric>();
				String[] expandedMetricStrs = StringUtils.tokenizeToStringArray(configString, ",", true, true);
				for (String expandedMetricStr : expandedMetricStrs) {
					set.add(ExpandedMetric.valueOf(expandedMetricStr));
				}
				config = new MetricExpansionConfig(set);
			}
			reporter.setExpansionConfig(config);
		}
		else if (hasProperty(EXPANSION_CONFIG_REF)) {
			reporter.setExpansionConfig(getProperty(EXPANSION_CONFIG, MetricExpansionConfig.class));
		}

		if (hasProperty(HTTP_POSTER_REF)) {
			reporter.setHttpPoster(getPropertyRef(HTTP_POSTER_REF, HttpPoster.class));
		}

		if (hasProperty(HTTP_CLIENT_CONFIG_REF)) {
			reporter.setHttpClientConfig(getPropertyRef(HTTP_CLIENT_CONFIG_REF, AsyncHttpClientConfig.class));
		}

		if (hasProperty(DELETE_IDLE_STATS)) {
			reporter.setDeleteIdleStats(getPropertyRef(DELETE_IDLE_STATS, boolean.class));
		}

		if (hasProperty(OMIT_COMPLEX_GAUGES)) {
			reporter.setOmitComplexGauges(getPropertyRef(OMIT_COMPLEX_GAUGES, boolean.class));
		}

		reporter.setPrefix(getPrefix());

		if (hasProperty(PREFIX_DELIMITER)) {
			reporter.setPrefixDelimiter(getProperty(PREFIX_DELIMITER));
		}

		if (hasProperty(DURATION_UNIT)) {
			reporter.setDurationUnit(getProperty(DURATION_UNIT, TimeUnit.class));
		}

		if (hasProperty(RATE_UNIT)) {
			reporter.setRateUnit(getProperty(RATE_UNIT, TimeUnit.class));
		}

		if (hasProperty(CLOCK_REF)) {
			reporter.setClock(getPropertyRef(CLOCK_REF, Clock.class));
		}

		if (hasProperty(SOURCE_REGEX)) {
			reporter.setSourceRegex(Pattern.compile(getProperty(SOURCE_REGEX)));
		}

		reporter.setFilter(getMetricFilter());

		return reporter.build();
	}

	@Override
	protected long getPeriod() {
		return convertDurationString(getProperty(PERIOD));
	}

}
