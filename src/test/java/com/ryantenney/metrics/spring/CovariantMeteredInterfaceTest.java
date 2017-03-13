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

public class CovariantMeteredInterfaceTest {

	ClassPathXmlApplicationContext ctx;
	MetricRegistry metricRegistry;

	@Before
	public void init() {
		this.ctx = new ClassPathXmlApplicationContext("classpath:covariant-metered-interface.xml");
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

	public interface MeteredInterface {

		@Timed
		public Number timedMethod();

		@Metered
		public Number meteredMethod();

		@ExceptionMetered
		public Number exceptionMeteredMethod() throws Throwable;

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

	}

	@SuppressWarnings("serial")
	public static class BogusException extends Throwable {}


}
