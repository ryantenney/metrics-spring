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

import java.nio.charset.Charset;
import java.util.concurrent.TimeUnit;

import javax.net.SocketFactory;

import com.codahale.metrics.Clock;
import com.codahale.metrics.graphite.Graphite;
import com.codahale.metrics.graphite.GraphiteRabbitMQ;
import com.codahale.metrics.graphite.GraphiteReporter;
import com.codahale.metrics.graphite.GraphiteSender;
import com.codahale.metrics.graphite.GraphiteUDP;
import com.codahale.metrics.graphite.PickledGraphite;
import com.rabbitmq.client.ConnectionFactory;

public class GraphiteReporterFactoryBean extends AbstractScheduledReporterFactoryBean<GraphiteReporter> {

	// Required
	public static final String HOST = "host";
	public static final String PORT = "port";
	public static final String PERIOD = "period";

	// Optional
	public static final String TRANSPORT = "transport";
	public static final String CHARSET = "charset";
	public static final String CLOCK_REF = "clock-ref";
	public static final String DURATION_UNIT = "duration-unit";
	public static final String RATE_UNIT = "rate-unit";

	// Pickle Optional
	public static final String BATCH_SIZE = "batch-size";

	// RabbitMQ Required
	public static final String CONNECTION_FACTORY_REF = "connection-factory-ref";
	public static final String EXCHANGE = "exchange";

	@Override
	public Class<GraphiteReporter> getObjectType() {
		return GraphiteReporter.class;
	}

	@SuppressWarnings("resource")
	@Override
	protected GraphiteReporter createInstance() {
		final GraphiteReporter.Builder reporter = GraphiteReporter.forRegistry(getMetricRegistry());

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
		reporter.prefixedWith(getPrefix());

		final String transport = getProperty(TRANSPORT, "tcp");
		final Charset charset = Charset.forName(getProperty(CHARSET, "UTF-8"));
		final GraphiteSender graphite;

		if ("rabbitmq".equals(transport)) {
			ConnectionFactory connectionFactory = getPropertyRef(CONNECTION_FACTORY_REF, ConnectionFactory.class);
			String exchange = getProperty(EXCHANGE);
			graphite = new GraphiteRabbitMQ(connectionFactory, exchange);
		}
		else {
			final String hostname = getProperty(HOST);
			final int port = getProperty(PORT, Integer.TYPE);

			if ("tcp".equals(transport)) {
				graphite = new Graphite(hostname, port, SocketFactory.getDefault(), charset);
			}
			else if ("udp".equals(transport)) {
				graphite = new GraphiteUDP(hostname, port);
			}
			else if ("pickle".equals(transport)) {
				graphite = new PickledGraphite(hostname, port, SocketFactory.getDefault(), charset, getProperty(BATCH_SIZE, Integer.TYPE, 100));
			}
			else {
				throw new IllegalArgumentException("Invalid graphite transport: " + transport);
			}
		}

		return reporter.build(graphite);
	}

	@Override
	protected long getPeriod() {
		return convertDurationString(getProperty(PERIOD));
	}

}
