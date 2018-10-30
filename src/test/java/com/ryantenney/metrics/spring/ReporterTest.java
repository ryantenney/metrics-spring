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

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.lessThanOrEqualTo;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Collection;

import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.CsvReporter;
import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.SharedMetricRegistries;
import com.codahale.metrics.Slf4jReporter;
import com.codahale.metrics.graphite.GraphiteReporter;
import com.codahale.metrics.jmx.JmxReporter;
import com.palominolabs.metrics.newrelic.NewRelicReporter;
import com.ryantenney.metrics.spring.reporter.FakeReporter;
import com.ryantenney.metrics.spring.reporter.MetricPrefixSupplier;

import org.coursera.metrics.datadog.DatadogReporter;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class ReporterTest {

	private static final String TEST_PREFIX = "test.i-001a391f";

	@SuppressWarnings("resource")
	@Test
	public void fakeReporters() throws Throwable {
		ClassPathXmlApplicationContext ctx = null;
		FakeReporter one = null;
		FakeReporter two = null;
		try {
			final MetricRegistry registry = SharedMetricRegistries.getOrCreate("reporterTestRegistry");

			ctx = new ClassPathXmlApplicationContext("classpath:fake-reporter-test.xml");
			ctx.start();

			Thread.sleep(1000);

			one = ctx.getBean("fakeReporterOne", FakeReporter.class);
			Assert.assertNotNull(one);
			Assert.assertFalse(AopUtils.isAopProxy(one.getRegistry()));
			Assert.assertSame(registry, one.getRegistry());
			Assert.assertEquals("milliseconds", one.getDurationUnit());
			Assert.assertEquals("second", one.getRateUnit());
			Assert.assertEquals(100000000, one.getPeriod());
			Assert.assertThat(one.getCalls(), allOf(greaterThanOrEqualTo(9), lessThanOrEqualTo(11)));
			Assert.assertEquals("[MetricFilter regex=foo]", one.getFilter().toString());
			Assert.assertEquals("some.crummy.prefix", one.getPrefix());
			Assert.assertTrue(one.isRunning());

			two = ctx.getBean("fakeReporterTwo", FakeReporter.class);
			Assert.assertNotNull(two);
			Assert.assertFalse(AopUtils.isAopProxy(two.getRegistry()));
			Assert.assertSame(registry, two.getRegistry());
			Assert.assertEquals("nanoseconds", two.getDurationUnit());
			Assert.assertEquals("hour", two.getRateUnit());
			Assert.assertEquals(100000000, two.getPeriod());
			Assert.assertThat(two.getCalls(), allOf(greaterThanOrEqualTo(9), lessThanOrEqualTo(11)));
			Assert.assertEquals(ctx.getBean(BarFilter.class), two.getFilter());
			Assert.assertEquals(TEST_PREFIX, two.getPrefix());
			Assert.assertTrue(two.isRunning());

			Assert.assertNull(ctx.getBean("fakeReporterThree", FakeReporter.class));

			// Make certain reporters aren't candidates for autowiring
			ReporterCollaborator collab = ctx.getBean(ReporterCollaborator.class);
			Assert.assertNotNull(collab.metricRegistry);
			Assert.assertNull(collab.fakeReporter);

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
			Assert.assertNotNull(ctx.getBean(CsvReporter.class));
			Assert.assertNotNull(ctx.getBean(JmxReporter.class));
			Assert.assertNotNull(ctx.getBean(Slf4jReporter.class));
			//Assert.assertNotNull(ctx.getBean(GangliaReporter.class));
			Assert.assertNotNull(ctx.getBean(NewRelicReporter.class));
			Assert.assertNotNull(ctx.getBean(DatadogReporter.class));

			Assert.assertNotNull(ctx.getBean("graphite", GraphiteReporter.class));
			Assert.assertNotNull(ctx.getBean("graphite-tcp", GraphiteReporter.class));
			Assert.assertNotNull(ctx.getBean("graphite-udp", GraphiteReporter.class));
			Assert.assertNotNull(ctx.getBean("graphite-pickle", GraphiteReporter.class));
			Assert.assertNotNull(ctx.getBean("graphite-rabbitmq", GraphiteReporter.class));
		}
		finally {
			if (ctx != null) {
				ctx.close();
			}
		}
	}

	@Test
	public void reportersPropertyPlaceholders() throws Throwable {
		ClassPathXmlApplicationContext ctx = null;
		try {
			ctx = new ClassPathXmlApplicationContext("classpath:reporter-placeholder-test.xml");

			@SuppressWarnings("resource")
			FakeReporter reporter = ctx.getBean(FakeReporter.class);
			Assert.assertEquals("nanoseconds", reporter.getDurationUnit());
			Assert.assertEquals("hour", reporter.getRateUnit());
			Assert.assertEquals(100000000, reporter.getPeriod());
			Assert.assertSame(ctx.getBean(BarFilter.class), reporter.getFilter());
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

	public static class TestMetricPrefixSupplier implements MetricPrefixSupplier {

		@Override
		public String getPrefix() {
			return TEST_PREFIX;
		}

	}

	public static class ReporterCollaborator {

		@Autowired(required = false)
		Collection<FakeReporter> fakeReporter;

		@Autowired
		MetricRegistry metricRegistry;

	}

}
