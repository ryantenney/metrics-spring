/*
 * Copyright 2012 Ryan W Tenney (http://ryan.10e.us)
 *            and Martello Technologies (http://martellotech.com)
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
package com.ryantenney.metrics.spring;

import static org.junit.Assert.*;

import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.ryantenney.metrics.annotation.InjectedMetric;

import com.yammer.metrics.core.Counter;
import com.yammer.metrics.core.Histogram;
import com.yammer.metrics.core.Meter;
import com.yammer.metrics.core.Metric;
import com.yammer.metrics.core.MetricName;
import com.yammer.metrics.core.MetricsRegistry;
import com.yammer.metrics.core.Timer;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:injected-metrics.xml")
public class InjectedMetricTest {

	@Autowired
	InjectedMetricTest.Target target;

	@Autowired
	MetricsRegistry metricsRegistry;

	@Test
	public void targetIsNotNull() {
		assertNotNull(target);

		Map<MetricName, Metric> metrics = metricsRegistry.getAllMetrics();

		assertNotNull(target.theNameForTheMeter);
		Meter meter = (Meter) metrics.get(new MetricName(InjectedMetricTest.Target.class, "theNameForTheMeter"));
		assertSame(target.theNameForTheMeter, meter);

		assertNotNull(target.timer);
		Timer timer = (Timer) metrics.get(new MetricName(InjectedMetricTest.Target.class, "theNameForTheTimer"));
		assertSame(target.timer, timer);

		assertNotNull(target.counter);
		Counter ctr = (Counter) metrics.get(new MetricName("group", "type", "counter"));
		assertSame(target.counter, ctr);

		assertNotNull(target.unbiasedHistogram);
		Histogram hist1 = (Histogram) metrics.get(new MetricName(InjectedMetricTest.Target.class, "unbiasedHistogram"));
		assertSame(target.unbiasedHistogram, hist1);

		assertNotNull(target.biasedHistogram);
		Histogram hist2 = (Histogram) metrics.get(new MetricName(InjectedMetricTest.Target.class, "biasedHistogram"));
		assertSame(target.biasedHistogram, hist2);
	}

	public static class Target {

		@InjectedMetric
		public Meter theNameForTheMeter;

		@InjectedMetric(name = "theNameForTheTimer")
		private Timer timer;

		@InjectedMetric(group = "group", type = "type")
		Counter counter;

		@InjectedMetric
		Histogram unbiasedHistogram;

		@InjectedMetric(biased = true)
		Histogram biasedHistogram;

		public Timer getTimer() {
			return timer;
		}

	}

}
