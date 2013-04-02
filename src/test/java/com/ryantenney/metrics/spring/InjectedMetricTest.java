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
import static com.ryantenney.metrics.spring.TestUtil.*;

import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.ryantenney.metrics.annotation.InjectedMetric;

import com.yammer.metrics.Counter;
import com.yammer.metrics.Histogram;
import com.yammer.metrics.Meter;
import com.yammer.metrics.Metric;
import com.yammer.metrics.MetricRegistry;
import com.yammer.metrics.Timer;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:injected-metrics.xml")
public class InjectedMetricTest {

	@Autowired
	InjectedMetricTest.Target target;

	@Autowired
	MetricRegistry metricRegistry;

	@Test
	public void targetIsNotNull() {
		assertNotNull(target);

		assertNotNull(target.theNameForTheMeter);
		Meter meter = (Meter) forInjectedMetricField(metricRegistry, InjectedMetricTest.Target.class, "theNameForTheMeter");
		assertSame(target.theNameForTheMeter, meter);

		assertNotNull(target.timer);
        Timer timer = (Timer) forInjectedMetricField(metricRegistry, InjectedMetricTest.Target.class, "timer");
		assertSame(target.timer, timer);

		assertNotNull(target.counter);
		Counter ctr = (Counter) forInjectedMetricField(metricRegistry, InjectedMetricTest.Target.class, "counter");
		assertSame(target.counter, ctr);

		assertNotNull(target.histogram);
		Histogram hist = (Histogram) forInjectedMetricField(metricRegistry, InjectedMetricTest.Target.class, "histogram");
		assertSame(target.histogram, hist);
	}

	public static class Target {

		@InjectedMetric
		public Meter theNameForTheMeter;

		@InjectedMetric(name = "theNameForTheTimer")
		private Timer timer;

		@InjectedMetric(name = "group.type.counter", absolute = true)
		Counter counter;

		@InjectedMetric
		Histogram histogram;

	}

}
