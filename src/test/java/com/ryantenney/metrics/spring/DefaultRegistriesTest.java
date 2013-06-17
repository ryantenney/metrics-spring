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
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheckRegistry;

public class DefaultRegistriesTest {

	@Test(expected=NoSuchBeanDefinitionException.class)
	public void testNoDefaultMetricRegistry() {
		ClassPathXmlApplicationContext ctx = null;
		try {
			ctx = new ClassPathXmlApplicationContext("classpath:default-registries.xml");
			ctx.getBean(MetricRegistry.class);
		}
		finally {
			if (ctx != null) {
				ctx.close();
			}
		}
	}

	@Test(expected=NoSuchBeanDefinitionException.class)
	public void testNoDefaultHealthCheckRegistry() {
		ClassPathXmlApplicationContext ctx = null;
		try {
			ctx = new ClassPathXmlApplicationContext("classpath:default-registries.xml");
			ctx.getBean(HealthCheckRegistry.class);
		}
		finally {
			if (ctx != null) {
				ctx.close();
			}
		}
	}

}
