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

import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.concurrent.TimeUnit;

import javax.net.SocketFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

import com.codahale.metrics.Clock;
import com.codahale.metrics.graphite.Graphite;
import com.codahale.metrics.graphite.GraphiteReporter;

public class GraphiteReporterFactoryBean extends AbstractScheduledReporterFactoryBean<GraphiteReporter> {

	private static final Logger LOG = LoggerFactory.getLogger(GraphiteReporterFactoryBean.class);

	// Required
	public static final String HOST = "host";
	public static final String PORT = "port";
	public static final String PERIOD = "period";

	// Optional
	public static final String CHARSET = "charset";
	public static final String PREFIX = "prefix";
	public static final String CLOCK_REF = "clock-ref";
	public static final String DURATION_UNIT = "duration-unit";
	public static final String RATE_UNIT = "rate-unit";

	// GraphiteSender FQCN
	private static final String GRAPHITE_SENDER = "com.codahale.metrics.graphite.GraphiteSender";

	@Override
	public Class<GraphiteReporter> getObjectType() {
		return GraphiteReporter.class;
	}

	@SuppressWarnings("resource")
	@Override
	protected GraphiteReporter createInstance() throws Exception {
		final GraphiteReporter.Builder reporter = GraphiteReporter.forRegistry(getMetricRegistry());

		if (hasProperty(PREFIX)) {
			reporter.prefixedWith(getProperty(PREFIX));
		}

		if (hasProperty(CLOCK_REF)) {
			reporter.withClock(getPropertyRef(CLOCK_REF, Clock.class));
		}

		if (hasProperty(DURATION_UNIT)) {
			reporter.convertDurationsTo(getProperty(DURATION_UNIT, TimeUnit.class));
		}

		if (hasProperty(RATE_UNIT)) {
			reporter.convertRatesTo(getProperty(RATE_UNIT, TimeUnit.class));
		}

		reporter.filter(getMetricFilter());

		final Charset charset;
		if (hasProperty(CHARSET)) {
			charset = Charset.forName(getProperty(CHARSET));
		}
		else {
			charset = Charset.forName("UTF-8");
		}

		// This resolves the hostname as the Graphite reporter is using the address, not the host
		final InetSocketAddress address = new InetSocketAddress(getProperty(HOST), getProperty(PORT, Integer.TYPE));
		final SocketFactory socketFactory = SocketFactory.getDefault();
		final Graphite graphite = new Graphite(address, socketFactory, charset);

		// I broke binary compatibility in Metrics 3.1 by introducing GraphiteSender
		if (ClassUtils.isPresent(GRAPHITE_SENDER, Graphite.class.getClassLoader())) {
			Class<?> graphiteSender = ClassUtils.forName(GRAPHITE_SENDER, Graphite.class.getClassLoader());
			Method buildMethod = ClassUtils.getMethodIfAvailable(GraphiteReporter.Builder.class, "build", graphiteSender);
			if (buildMethod != null && ClassUtils.isAssignableValue(graphiteSender, graphite)) {
				LOG.info("Metrics 3.1 detected, invoking GraphiteReporter build method via reflection");
				return (GraphiteReporter) ReflectionUtils.invokeMethod(buildMethod, reporter, graphite);
			}
		}

		return reporter.build(graphite);
	}

	@Override
	protected long getPeriod() {
		return convertDurationString(getProperty(PERIOD));
	}

}
