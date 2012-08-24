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
