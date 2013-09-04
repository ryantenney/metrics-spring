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

import org.junit.Assert;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.SharedMetricRegistries;

public class SharedRegistryTest {

	@Test
	public void testDefaultRegistries() {
		ClassPathXmlApplicationContext ctx = null;
		try {
			MetricRegistry instance = new MetricRegistry();
			SharedMetricRegistries.add("SomeSharedRegistry", instance);
			ctx = new ClassPathXmlApplicationContext("classpath:shared-registry.xml");
			Assert.assertSame("Should be the same MetricRegistry", instance, ctx.getBean(MetricRegistry.class));
		}
		finally {
			if (ctx != null) {
				ctx.close();
			}
		}
	}

}
