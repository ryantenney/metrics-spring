package com.ryantenney.metrics.spring;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.yammer.metrics.annotation.ExceptionMetered;
import com.yammer.metrics.annotation.Metered;
import com.yammer.metrics.annotation.Timed;
import com.yammer.metrics.core.MetricsRegistry;
import com.yammer.metrics.Metrics;

/**
 * Purpose of test:
 * Verify that calling a method that is annotated at the interface
 * level but not the implementation level doesn't throw an NPE.
 * Also verifies that it doesn't register any metrics.
 */
public class MeteredInterfaceTest {

	ClassPathXmlApplicationContext ctx;
	MetricsRegistry metricsRegistry;

	@Before
	public void init() {
		this.ctx = new ClassPathXmlApplicationContext("classpath:metered-interface.xml");
		this.metricsRegistry = this.ctx.getBean(MetricsRegistry.class);
	}

	@After
	public void destroy() throws Throwable {
		this.ctx.stop();
	}

	@Test
	public void notUsingDefaultMetricsRegistry() {
		Assert.assertNotSame("For the purpose of this test we cannot use the default registry!", Metrics.defaultRegistry(), this.metricsRegistry);
		Assert.assertTrue("No metrics registered", this.metricsRegistry.getAllMetrics().isEmpty());
	}

	@Test
	public void testMeteredInterface() {
		MeteredInterface mi = ctx.getBean(MeteredInterface.class);
		Assert.assertNotNull("Expected to be able to get MeteredInterface by interface and not by class.", mi);
	}

	@Test(expected=NoSuchBeanDefinitionException.class)
	public void testMeteredInterfaceImpl() {
		MeteredInterfaceImpl mc = ctx.getBean(MeteredInterfaceImpl.class);
		Assert.assertNull("Expected to be unable to get MeteredInterfaceImpl by class.", mc);
	}

	@Test
	public void testTimedMethod() {
		ctx.getBean(MeteredInterface.class).timedMethod();
		Assert.assertTrue("No metrics should be registered", this.metricsRegistry.getAllMetrics().isEmpty());
	}

	@Test
	public void testMeteredMethod() {
		ctx.getBean(MeteredInterface.class).meteredMethod();
		Assert.assertTrue("No metrics should be registered", this.metricsRegistry.getAllMetrics().isEmpty());
	}

	@Test(expected=BogusException.class)
	public void testExceptionMeteredMethod() throws Throwable {
		try {
			ctx.getBean(MeteredInterface.class).exceptionMeteredMethod();
		} catch (Throwable t) {
			Assert.assertTrue("No metrics should be registered", this.metricsRegistry.getAllMetrics().isEmpty());
			throw t;
		}
	}


	public interface MeteredInterface {

		@Timed
		public void timedMethod();

		@Metered
		public void meteredMethod();

		@ExceptionMetered
		public void exceptionMeteredMethod() throws Throwable;

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

	}


	public static class BogusException extends Throwable {}


}
