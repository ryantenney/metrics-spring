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

import com.codahale.metrics.*;
import org.hamcrest.CoreMatchers;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.codahale.metrics.annotation.ExceptionMetered;
import com.codahale.metrics.annotation.Metered;
import com.codahale.metrics.annotation.Timed;
import com.ryantenney.metrics.annotation.Counted;

import java.util.SortedSet;

/**
 * Purpose of test:
 * Verify that calling a method that is annotated at the interface
 * level but not the implementation level doesn't throw an NPE.
 * Also verifies that it doesn't register any metrics.
 */
public class MeteredInterfaceTest {
	public static final String COUNTER_NAME = "com.ryantenney.metrics.spring.MeteredInterfaceTest.MeteredInterface.countedMethod";
	public static final String EXCEPTION_METER_NAME = "com.ryantenney.metrics.spring.MeteredInterfaceTest.MeteredInterface.exceptionMeteredMethod.exceptions";
	public static final String METER_NAME = "com.ryantenney.metrics.spring.MeteredInterfaceTest.MeteredInterface.meteredMethod";
	public static final String TIMER_NAME = "com.ryantenney.metrics.spring.MeteredInterfaceTest.MeteredInterface.timedMethod";

	ClassPathXmlApplicationContext ctx;
	MetricRegistry metricRegistry;

	@Before
	public void init() {
		this.ctx = new ClassPathXmlApplicationContext("classpath:metered-interface.xml");
		this.metricRegistry = this.ctx.getBean(MetricRegistry.class);
	}

	@After
	public void destroy() throws Throwable {
		this.ctx.stop();
	}

	@Test
	public void allMetricsRegistered() {
		SortedSet<String> metricNames = this.metricRegistry.getNames();
		Assert.assertThat("4 metrics present", metricNames, CoreMatchers.hasItems(
				COUNTER_NAME,
				EXCEPTION_METER_NAME,
				METER_NAME,
				TIMER_NAME
				));
	}

	@Test
	public void testMeteredInterface() {
		MeteredInterface mi = ctx.getBean(MeteredInterface.class);
		Assert.assertNotNull("Expected to be able to get MeteredInterface by interface and not by class.", mi);
	}

	@Test(expected = NoSuchBeanDefinitionException.class)
	public void testMeteredInterfaceImpl() {
		MeteredInterfaceImpl mc = ctx.getBean(MeteredInterfaceImpl.class);
		Assert.assertNull("Expected to be unable to get MeteredInterfaceImpl by class.", mc);
	}

	@Test
	public void testTimedMethod() {
		ctx.getBean(MeteredInterface.class).timedMethod();
		Timer timer = this.metricRegistry.getTimers().get(TIMER_NAME);
		Assert.assertNotNull("Timer should be registered", timer);
		Assert.assertEquals("Timer count should be 1", 1L, timer.getCount());
	}

	@Test
	public void testMeteredMethod() {
		ctx.getBean(MeteredInterface.class).meteredMethod();
		Meter meter = this.metricRegistry.getMeters().get(METER_NAME);
		Assert.assertNotNull("Meter should be registered", meter);
		Assert.assertEquals("Meter count should be 1", 1L, meter.getCount());
	}

	@Test(expected = BogusException.class)
	public void testExceptionMeteredMethod() throws Throwable {
		try {
			ctx.getBean(MeteredInterface.class).exceptionMeteredMethod();
		}
		catch (Throwable t) {
			Meter meter = this.metricRegistry.getMeters().get(EXCEPTION_METER_NAME);
			Assert.assertNotNull("Exception Meter should be registered", meter);
			Assert.assertEquals("Exception Meter count should be 1", 1L, meter.getCount());
			throw t;
		}
	}

	@Test
	public void testCountedMethod() {
		ctx.getBean(MeteredInterface.class).countedMethod();
		Counter counter = this.metricRegistry.getCounters().get(COUNTER_NAME);
		Assert.assertNotNull("Counter should be registered", counter);
		Assert.assertEquals("Counter count should be 1", 1L, counter.getCount());
	}

	public interface MeteredInterface {

		@Timed
		public void timedMethod();

		@Metered
		public void meteredMethod();

		@ExceptionMetered(cause = BogusException.class)
		public void exceptionMeteredMethod() throws Throwable;

		@Counted(monotonic = true)
		public void countedMethod();

	}

	public static class MeteredInterfaceImpl implements MeteredInterface {

		@Override
		public void timedMethod() {}

		@Override
		public void meteredMethod() {}

		@Override
		public void exceptionMeteredMethod() throws Throwable {
			throw new BogusException();
		}

		@Override
		public void countedMethod() {}

	}

	@SuppressWarnings("serial")
	public static class BogusException extends Throwable {}

}
