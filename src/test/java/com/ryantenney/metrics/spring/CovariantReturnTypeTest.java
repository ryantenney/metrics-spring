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

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.annotation.ExceptionMetered;
import com.codahale.metrics.annotation.Metered;
import com.codahale.metrics.annotation.Timed;
import com.ryantenney.metrics.annotation.Counted;

public class CovariantReturnTypeTest {

	ClassPathXmlApplicationContext ctx;
	MetricRegistry metricRegistry;

	@Before
	public void init() {
		this.ctx = new ClassPathXmlApplicationContext("classpath:covariant-return-type.xml");
		this.metricRegistry = this.ctx.getBean(MetricRegistry.class);
	}

	@After
	public void destroy() throws Throwable {
		this.ctx.stop();
	}

	@Test
	public void noMetricsRegistered() {
		Assert.assertTrue("No metrics registered", this.metricRegistry.getNames().isEmpty());
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
		Assert.assertTrue("No metrics should be registered", this.metricRegistry.getNames().isEmpty());
	}

	@Test
	public void testMeteredMethod() {
		ctx.getBean(MeteredInterface.class).meteredMethod();
		Assert.assertTrue("No metrics should be registered", this.metricRegistry.getNames().isEmpty());
	}

	@Test(expected = BogusException.class)
	public void testExceptionMeteredMethod() throws Throwable {
		try {
			ctx.getBean(MeteredInterface.class).exceptionMeteredMethod();
		}
		catch (Throwable t) {
			Assert.assertTrue("No metrics should be registered", this.metricRegistry.getNames().isEmpty());
			throw t;
		}
	}

	@Test
	public void testCountedMethod() {
		ctx.getBean(MeteredInterface.class).countedMethod();
		Assert.assertTrue("No metrics should be registered", this.metricRegistry.getNames().isEmpty());
	}

	public interface MeteredInterface {

		@Timed
		public Number timedMethod();

		@Metered
		public Number meteredMethod();

		@ExceptionMetered
		public Number exceptionMeteredMethod() throws Throwable;

		@Counted
		public Number countedMethod();

	}

	public static class MeteredInterfaceImpl implements MeteredInterface {

		@Override
		public Integer timedMethod() { return 0; }

		@Override
		public Long meteredMethod() { return 0L; }

		@Override
		public Byte exceptionMeteredMethod() throws Throwable {
			throw new BogusException();
		}

		@Override
		public Double countedMethod() { return 0.0; }

	}

	@SuppressWarnings("serial")
	public static class BogusException extends Throwable {}


}
