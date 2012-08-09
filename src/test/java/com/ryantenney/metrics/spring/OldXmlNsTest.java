package com.ryantenney.metrics.spring;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.yammer.metrics.HealthChecks;
import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.HealthCheckRegistry;
import com.yammer.metrics.core.MetricsRegistry;

/**
 * Purpose of test:
 * Verifies that the old xmlns/schemaLocation at yammer.com still works
 */
public class OldXmlNsTest {

	@Test
	public void testOldXmlNs() {
		ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("classpath:old-xmlns.xml");
		Assert.assertSame("Should be default MetricsRegistry.", Metrics.defaultRegistry(), ctx.getBean(MetricsRegistry.class));
		Assert.assertSame("Should be default HealthCheckRegistry.", HealthChecks.defaultRegistry(), ctx.getBean(HealthCheckRegistry.class));
	}

}
