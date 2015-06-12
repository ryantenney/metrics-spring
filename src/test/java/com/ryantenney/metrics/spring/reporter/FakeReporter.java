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

import java.util.SortedMap;
import java.util.concurrent.TimeUnit;

import io.dropwizard.metrics.Counter;
import io.dropwizard.metrics.Gauge;
import io.dropwizard.metrics.Histogram;
import io.dropwizard.metrics.Meter;
import io.dropwizard.metrics.MetricFilter;
import io.dropwizard.metrics.MetricName;
import io.dropwizard.metrics.MetricRegistry;
import io.dropwizard.metrics.ScheduledReporter;
import io.dropwizard.metrics.Timer;

public class FakeReporter extends ScheduledReporter {

	private final MetricRegistry registry;
	private final MetricFilter filter;

	private long period;
	private int calls = 0;
	private boolean running = false;

	public FakeReporter(MetricRegistry registry, MetricFilter filter, TimeUnit rateUnit, TimeUnit durationUnit) {
		super(registry, "test-reporter", filter, rateUnit, durationUnit);
		this.registry = registry;
		this.filter = filter;
	}

	public MetricRegistry getRegistry() {
		return registry;
	}

	public MetricFilter getFilter() {
		return filter;
	}

	@Override
	public String getRateUnit() {
		return super.getRateUnit();
	}

	@Override
	public String getDurationUnit() {
		return super.getDurationUnit();
	}

	public long getPeriod() {
		return period;
	}

	public int getCalls() {
		return calls;
	}

	public boolean isRunning() {
		return running;
	}

	@Override
	public void start(final long period, final TimeUnit unit) {
		super.start(period, unit);
		this.period = unit.toNanos(period);
		this.running = true;
	}

	@Override
	public void stop() {
		super.stop();
		this.running = false;
	}

	@Override
	@SuppressWarnings("rawtypes")
	public void report(SortedMap<MetricName, Gauge> gauges, SortedMap<MetricName, Counter> counters, SortedMap<MetricName, Histogram> histograms,
			SortedMap<MetricName, Meter> meters, SortedMap<MetricName, Timer> timers) {
		calls++;
	}

}
