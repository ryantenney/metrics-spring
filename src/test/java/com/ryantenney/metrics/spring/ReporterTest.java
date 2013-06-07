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

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.JmxReporter;
import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.SharedMetricRegistries;
import com.codahale.metrics.Slf4jReporter;
import com.codahale.metrics.ganglia.GangliaReporter;
import com.codahale.metrics.graphite.GraphiteReporter;
import com.ryantenney.metrics.spring.reporter.FakeReporter;

public class ReporterTest {

	@Test
	public void fakeReporters() throws Throwable {
		ClassPathXmlApplicationContext ctx = null;
		FakeReporter one = null;
		FakeReporter two = null;
		try {
			MetricRegistry registry = new MetricRegistry();
			SharedMetricRegistries.add("reporterTestRegistry", registry);

			ctx = new ClassPathXmlApplicationContext("classpath:fake-reporter-test.xml");
			ctx.start();

			Thread.sleep(1000);

			one = ctx.getBean("fakeReporterOne", FakeReporter.class);
			Assert.assertEquals(registry, one.getRegistry());
			Assert.assertEquals("milliseconds", one.getDurationUnit());
			Assert.assertEquals("second", one.getRateUnit());
			Assert.assertEquals(100000000, one.getPeriod());
			Assert.assertEquals(10, one.getCalls());
			Assert.assertEquals("[MetricFilter regex=foo]", one.getFilter().toString());
			Assert.assertTrue(one.isRunning());

			two = ctx.getBean("fakeReporterTwo", FakeReporter.class);
			Assert.assertEquals(registry, two.getRegistry());
			Assert.assertEquals("nanoseconds", two.getDurationUnit());
			Assert.assertEquals("hour", two.getRateUnit());
			Assert.assertEquals(100000000, two.getPeriod());
			Assert.assertEquals(10, one.getCalls());
			Assert.assertEquals(ctx.getBean(BarFilter.class), two.getFilter());
			Assert.assertTrue(one.isRunning());

		}
		finally {
			if (ctx != null) {
				ctx.stop();
				ctx.close();
			}
		}

		if (one != null) {
			Assert.assertFalse(one.isRunning());
		}

		if (two != null) {
			Assert.assertFalse(two.isRunning());
		}
	}

	@Test
	public void realReporters() throws Throwable {
		ClassPathXmlApplicationContext ctx = null;
		try {
			ctx = new ClassPathXmlApplicationContext("classpath:reporter-test.xml");
			// intentionally avoids calling ctx.start()

			Assert.assertNotNull(ctx.getBean(ConsoleReporter.class));
			Assert.assertNotNull(ctx.getBean(JmxReporter.class));
			Assert.assertNotNull(ctx.getBean(Slf4jReporter.class));
			Assert.assertNotNull(ctx.getBean(GraphiteReporter.class));
			Assert.assertNotNull(ctx.getBean(GangliaReporter.class));
		}
		finally {
			if (ctx != null) {
				ctx.close();
			}
		}
	}

	public static PrintStream testPrintStream() {
		return new PrintStream(new ByteArrayOutputStream());
	}

	public static class BarFilter implements MetricFilter {

		@Override
		public boolean matches(String name, Metric metric) {
			return false;
		}

	}

}
