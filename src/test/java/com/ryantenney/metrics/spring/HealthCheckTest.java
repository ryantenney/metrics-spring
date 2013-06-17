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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.codahale.metrics.health.HealthCheck;
import com.codahale.metrics.health.HealthCheck.Result;
import com.codahale.metrics.health.HealthCheckRegistry;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:health-check.xml")
public class HealthCheckTest {

	@Autowired
	HealthCheckClass healthCheckClass;

	@Autowired
	HealthCheckRegistry healthCheckRegistry;

	@Test
	public void testHealthy() {
		healthCheckClass.setShouldFail(false);
		Result result = healthCheckRegistry.runHealthCheck("myHealthCheckClass");
		assertTrue(result.isHealthy());
	}

	@Test
	public void testUnhealthy() {
		healthCheckClass.setShouldFail(true);
		Result result = healthCheckRegistry.runHealthCheck("myHealthCheckClass");
		assertFalse(result.isHealthy());
		assertEquals("fail whale", result.getMessage());
	}

	public static class HealthCheckClass extends HealthCheck {

		private boolean shouldFail;

		public boolean isShouldFail() {
			return shouldFail;
		}

		public void setShouldFail(boolean shouldFail) {
			this.shouldFail = shouldFail;
		}

		@Override
		protected Result check() {
			if (shouldFail) {
				return Result.unhealthy(new RuntimeException("fail whale"));
			}
			return Result.healthy();
		}

	}

}
