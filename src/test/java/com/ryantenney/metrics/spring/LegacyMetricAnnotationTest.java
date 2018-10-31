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
package com.ryantenney.metrics.spring;

import static com.ryantenney.metrics.spring.TestUtil.forLegacyMetricField;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.codahale.metrics.UniformReservoir;
import com.ryantenney.metrics.annotation.Metric;

@SuppressWarnings("deprecation")
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:legacy-metric-annotation.xml")
public class LegacyMetricAnnotationTest {

	@Autowired
	@Qualifier("target1")
	LegacyMetricAnnotationTest.Target target;

	@Autowired
	@Qualifier("target2")
	LegacyMetricAnnotationTest.Target target2;

	@Autowired
	MetricRegistry metricRegistry;

	@Test
	public void targetIsNotNull() throws Exception {
		assertNotNull(target);
		assertNotNull(target2);

		assertNotNull(target.theNameForTheMeter);
		assertNotNull(target2.theNameForTheMeter);
		Meter meter = (Meter) forLegacyMetricField(metricRegistry, LegacyMetricAnnotationTest.Target.class, "theNameForTheMeter");
		assertSame(target.theNameForTheMeter, meter);
		assertSame(target2.theNameForTheMeter, meter);

		assertNotNull(target.timer);
		assertNotNull(target2.timer);
		Timer timer = (Timer) forLegacyMetricField(metricRegistry, LegacyMetricAnnotationTest.Target.class, "timer");
		assertSame(target.timer, timer);
		assertSame(target2.timer, timer);

		assertNotNull(target.counter);
		assertNotNull(target2.counter);
		Counter ctr = (Counter) forLegacyMetricField(metricRegistry, LegacyMetricAnnotationTest.Target.class, "counter");
		assertSame(target.counter, ctr);
		assertSame(target2.counter, ctr);

		assertNotNull(target.histogram);
		assertNotNull(target2.histogram);
		Histogram hist = (Histogram) forLegacyMetricField(metricRegistry, LegacyMetricAnnotationTest.Target.class, "histogram");
		assertSame(target.histogram, hist);
		assertSame(target2.histogram, hist);

		assertNotNull(target.uniformHistogram);
		assertNotNull(target2.uniformHistogram);
		Histogram uniHist = (Histogram) forLegacyMetricField(metricRegistry, LegacyMetricAnnotationTest.Target.class, "uniformHistogram");
		assertSame(target.uniformHistogram, uniHist);
		assertSame(target2.uniformHistogram, uniHist);
	}

	public static class Target {

		@Metric
		public Meter theNameForTheMeter;

		@Metric(name = "theNameForTheTimer")
		private Timer timer;

		@Metric(name = "group.type.counter", absolute = true)
		Counter counter;

		@Metric
		Histogram histogram;

		@Metric
		Histogram uniformHistogram = new Histogram(new UniformReservoir());

	}

}
