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

import static com.ryantenney.metrics.spring.TestUtil.forCountedMethod;
import static com.ryantenney.metrics.spring.TestUtil.forMeteredMethod;
import static com.ryantenney.metrics.spring.TestUtil.forTimedMethod;
import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.codahale.metrics.annotation.ExceptionMetered;
import com.codahale.metrics.annotation.Metered;
import com.codahale.metrics.annotation.Timed;
import com.ryantenney.metrics.annotation.Counted;

/**
 * Purpose of test:
 * Verify that calling a method from a class implementing an interface 
 * but annotated at the class level doesn't throw an NPE
 * Also verifies that it does register metrics.
 */
public class MeteredClassImpementsInterfaceTest {

	MeteredClassInterface meteredClass;

	ClassPathXmlApplicationContext ctx;
	MetricRegistry metricRegistry;

	@Before
	public void init() {
		this.ctx = new ClassPathXmlApplicationContext("classpath:metered-interface-impl.xml");
		this.metricRegistry = this.ctx.getBean(MetricRegistry.class);
		this.meteredClass = (MeteredClassInterface) this.ctx.getBean("metered-class-interface");
	}

	@After
	public void destroy() throws Throwable {
		this.ctx.stop();
	}

	@Test
	public void noMetricsRegistered() {
		Assert.assertFalse("No metrics registered", this.metricRegistry.getNames().isEmpty());
	}

	@Test
	public void testMeteredClassInterface() {
		MeteredClassInterface mi = ctx.getBean(MeteredClassInterface.class);
		Assert.assertNotNull("Expected to be able to get MeteredInterface by interface and not by class.", mi);
	}

	@Test(expected = NoSuchBeanDefinitionException.class)
	public void testMeteredInterfaceImpl() {
		MeteredClassInterface mc = ctx.getBean(MeteredClassImpl.class);
		Assert.assertNull("Expected to be unable to get MeteredInterfaceImpl by class.", mc);
	}

	@Test
	public void testTimedMethod() {
		ctx.getBean(MeteredClassInterface.class).timedMethod();
		Assert.assertFalse("Metrics should be registered", this.metricRegistry.getTimers().isEmpty());
	}

	@Test
	public void testMeteredMethod() {
		ctx.getBean(MeteredClassInterface.class).meteredMethod();
		Assert.assertFalse("Metrics should be registered", this.metricRegistry.getMeters().isEmpty());
	}

	@Test
	public void testCountedMethod() {
		ctx.getBean(MeteredClassInterface.class).countedMethod(null);
		Assert.assertFalse("Metrics should be registered", this.metricRegistry.getCounters().isEmpty());
	}

	@Test(expected = BogusException.class)
	public void testExceptionMeteredMethod() throws Throwable {
		try {
			ctx.getBean(MeteredClassInterface.class).exceptionMeteredMethod();
		}
		catch (Throwable t) {
			Assert.assertFalse("Metrics should be registered", this.metricRegistry.getMeters().isEmpty());
			throw t;
		}
	}

	@Test
	public void timedMethod() throws Throwable {
		Timer timedMethod = forTimedMethod(metricRegistry, MeteredClassImpl.class, "timedMethod");

		assertEquals(0, timedMethod.getCount());

		meteredClass.timedMethod();
		assertEquals(1, timedMethod.getCount());
	}

	@Test
	public void meteredMethod() throws Throwable {
		Meter meteredMethod = forMeteredMethod(metricRegistry, MeteredClassImpl.class, "meteredMethod");

		assertEquals(0, meteredMethod.getCount());

		meteredClass.meteredMethod();
		assertEquals(1, meteredMethod.getCount());
	}

	@Test
	public void countedMethod() throws Throwable {
		final Counter countedMethod = forCountedMethod(metricRegistry, MeteredClassImpl.class, "countedMethod");

		assertEquals(0, countedMethod.getCount());

		meteredClass.countedMethod(new Runnable() {
			@Override
			public void run() {
				assertEquals(1, countedMethod.getCount());
			}
		});

		assertEquals(0, countedMethod.getCount());
	}

	public interface MeteredClassInterface {

		public void timedMethod();

		public void meteredMethod();

		public void countedMethod(Runnable runnable);

		public void exceptionMeteredMethod() throws Throwable;

	}

	public static class MeteredClassImpl implements MeteredClassInterface {

		@Override
		@Timed
		public void timedMethod() {}

		@Override
		@Metered
		public void meteredMethod() {}

		@Override
		@Counted
		public void countedMethod(Runnable runnable) {
			if (runnable != null) runnable.run();
		}

		@Override
		@ExceptionMetered
		public void exceptionMeteredMethod() throws Throwable {
			throw new BogusException();
		}

	}

	@SuppressWarnings("serial")
	public static class BogusException extends Throwable {}

}
