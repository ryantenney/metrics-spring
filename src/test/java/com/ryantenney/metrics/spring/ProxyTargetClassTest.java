package com.ryantenney.metrics.spring;

import com.yammer.metrics.annotation.Timed;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Purpose of test:
 * Tests `proxy-target-class` option in `<metrics:annotation-driven />`
 */
public class ProxyTargetClassTest {

	@Test(expected = BeanCreationException.class)
	public void negativeContextLoadingTest() {
		new ClassPathXmlApplicationContext("classpath:proxy-target-class-disabled.xml");
	}

	@Test
	public void positiveContextLoadingTest() {
		ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("classpath:proxy-target-class-enabled.xml");
		Assert.assertNotNull("Expected to be able to get ProxyTargetClass by class.", ctx.getBean(ProxyTargetClass.class));
		Assert.assertNotNull("Expected to be able to get ProxyTargetClass from AutowiredCollaborator.", ctx.getBean(AutowiredCollaborator.class).getDependency());
	}


	/**
	 * Empty interface to trick Spring.
	 */
	public interface UselessInterface {}


	public static class ProxyTargetClass implements UselessInterface {

		@Timed
		public void timed() {}

	}


	public static class AutowiredCollaborator {

		@Autowired
		private ProxyTargetClass dependency;

		public ProxyTargetClass getDependency() {
			return dependency;
		}

	}


}
