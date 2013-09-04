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
package com.ryantenney.metrics.spring;

import static com.ryantenney.metrics.spring.TestUtil.forInjectMetricField;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.ryantenney.metrics.annotation.InjectMetric;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:injected-metrics.xml")
public class InjectMetricTest {

	@Autowired
	InjectMetricTest.Target target;

	@Autowired
	MetricRegistry metricRegistry;

	@Test
	public void targetIsNotNull() {
		assertNotNull(target);

		assertNotNull(target.theNameForTheMeter);
		Meter meter = (Meter) forInjectMetricField(metricRegistry, InjectMetricTest.Target.class, "theNameForTheMeter");
		assertSame(target.theNameForTheMeter, meter);

		assertNotNull(target.timer);
		Timer timer = (Timer) forInjectMetricField(metricRegistry, InjectMetricTest.Target.class, "timer");
		assertSame(target.timer, timer);

		assertNotNull(target.counter);
		Counter ctr = (Counter) forInjectMetricField(metricRegistry, InjectMetricTest.Target.class, "counter");
		assertSame(target.counter, ctr);

		assertNotNull(target.histogram);
		Histogram hist = (Histogram) forInjectMetricField(metricRegistry, InjectMetricTest.Target.class, "histogram");
		assertSame(target.histogram, hist);
	}

	public static class Target {

		@InjectMetric
		public Meter theNameForTheMeter;

		@InjectMetric(name = "theNameForTheTimer")
		private Timer timer;

		@InjectMetric(name = "group.type.counter", absolute = true)
		Counter counter;

		@InjectMetric
		Histogram histogram;

	}

}
