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

import org.junit.Assert;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheckRegistry;

public class RegistryTest {

	@Test
	public void testDefaultRegistries() {
		ClassPathXmlApplicationContext ctx = null;
		try {
			ctx = new ClassPathXmlApplicationContext("classpath:default-registries.xml");
			Assert.assertNotNull("Should have a MetricRegistry bean.", ctx.getBean(MetricRegistry.class));
			Assert.assertNotNull("Should have a HealthCheckRegistry bean.", ctx.getBean(HealthCheckRegistry.class));
		}
		finally {
			if (ctx != null) {
				ctx.close();
			}
		}
	}

	@Test
	public void testSuppliedRegistries() {
		ClassPathXmlApplicationContext ctx = null;
		try {
			ctx = new ClassPathXmlApplicationContext("classpath:supplied-registries.xml");
			Assert.assertNotNull("Should have a MetricRegistry bean.", ctx.getBean("metrics", MetricRegistry.class));
			Assert.assertNotNull("Should have a HealthCheckRegistry bean.", ctx.getBean("health", HealthCheckRegistry.class));
		}
		finally {
			if (ctx != null) {
				ctx.close();
			}
		}
	}

	@Test
	public void testCustomRegistries() {
		ClassPathXmlApplicationContext ctx = null;
		try {
			ctx = new ClassPathXmlApplicationContext("classpath:custom-registries.xml");
			Assert.assertSame("Should have a custom MetricRegistry bean.", MetricRegistry.class, ctx.getBean("metrics", MetricRegistry.class).getClass()
					.getSuperclass());
			Assert.assertSame("Should have a custom HealthCheckRegistry bean.", HealthCheckRegistry.class, ctx.getBean("health", HealthCheckRegistry.class)
					.getClass().getSuperclass());
		}
		finally {
			if (ctx != null) {
				ctx.close();
			}
		}
	}

	public static class CustomMetricRegistry extends MetricRegistry {}

	public static class CustomHealthCheckRegistry extends HealthCheckRegistry {}

}
