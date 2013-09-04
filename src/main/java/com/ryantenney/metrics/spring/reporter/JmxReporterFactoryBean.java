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

import com.codahale.metrics.JmxReporter;
import com.codahale.metrics.MetricFilter;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.SmartLifecycle;

import javax.management.MBeanServer;
import java.util.concurrent.TimeUnit;

public class JmxReporterFactoryBean extends AbstractReporterFactoryBean<JmxReporter> implements SmartLifecycle, DisposableBean {

	// Optional
	public static final String DOMAIN = "domain";
	public static final String DURATION_UNIT = "duration-unit";
	public static final String RATE_UNIT = "rate-unit";
	public static final String FILTER_PATTERN = "filter";
	public static final String FILTER_REF = "filter-ref";
	public static final String MBEAN_SERVER_REF = "mbean-server-ref";

	private boolean running = false;

	@Override
	public Class<JmxReporter> getObjectType() {
		return JmxReporter.class;
	}

	@Override
	protected JmxReporter createInstance() {
		final JmxReporter.Builder reporter = JmxReporter.forRegistry(getMetricRegistry());

		if (hasProperty(DOMAIN)) {
			reporter.inDomain(getProperty(DOMAIN));
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

		if (hasProperty(MBEAN_SERVER_REF)) {
			reporter.registerWith(getPropertyRef(MBEAN_SERVER_REF, MBeanServer.class));
		}

		return reporter.build();
	}

	@Override
	public void start() {
		if (!running) {
			getObject().start();
			running = true;
		}
	}

	@Override
	public void stop() {
		if (running) {
			getObject().stop();
			running = false;
		}
	}

	@Override
	public boolean isRunning() {
		return running;
	}

	@Override
	public void destroy() throws Exception {
		stop();
	}

    @Override
    public boolean isAutoStartup() {
        return true;
    }

    @Override
    public void stop(Runnable runnable) {
        stop();
        runnable.run();
    }

    @Override
    public int getPhase() {
        return 0;
    }
}
