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

import static com.ryantenney.metrics.spring.TestUtil.forLegacyCachedGaugeMethod;
import static com.ryantenney.metrics.spring.TestUtil.forLegacyCountedMethod;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.codahale.metrics.CachedGauge;
import com.codahale.metrics.Counter;
import com.codahale.metrics.MetricRegistry;
import com.ryantenney.metrics.annotation.Counted;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:legacy-annotation-metered-class.xml")
@SuppressWarnings("deprecation")
public class LegacyAnnotationMeteredClassTest {

	@Autowired
	LegacyMeteredClass meteredClass;

	MetricRegistry metricRegistry;

	@Autowired
	public void setMetricRegistry(MetricRegistry metricRegistry) {
		this.metricRegistry = metricRegistry;
		this.metricRegistry.addListener(new LoggingMetricRegistryListener());
	}

	@Test
	public void gauges() {
		CachedGauge<?> cachedGaugedMethod = forLegacyCachedGaugeMethod(metricRegistry, LegacyMeteredClass.class, "cachedGaugedMethod");

		assertEquals(999, cachedGaugedMethod.getValue());

		meteredClass.setGaugedField(1000);

		assertEquals(999, cachedGaugedMethod.getValue());
	}

	@Test
	public void countedMethod() throws Throwable {
		final Counter countedMethod = forLegacyCountedMethod(metricRegistry, LegacyMeteredClass.class, "countedMethod");

		assertEquals(0, countedMethod.getCount());

		meteredClass.countedMethod(new Runnable() {
			@Override
			public void run() {
				assertEquals(1, countedMethod.getCount());

				meteredClass.countedMethod(new Runnable() {
					@Override
					public void run() {
						assertEquals(2, countedMethod.getCount());
					}
				});

				assertEquals(1, countedMethod.getCount());
			}
		});

		assertEquals(0, countedMethod.getCount());
	}

	@Test
	public void monotonicCountedMethod() throws Throwable {
		final Counter countedMethod = forLegacyCountedMethod(metricRegistry, LegacyMeteredClass.class, "monotonicCountedMethod");

		assertEquals(0, countedMethod.getCount());

		meteredClass.monotonicCountedMethod();
		assertEquals(1, countedMethod.getCount());

		meteredClass.monotonicCountedMethod();
		assertEquals(2, countedMethod.getCount());
	}

	@Test
	public void overloadedCountedMethod() {
		final Counter overloaded = metricRegistry.getCounters().get(MetricRegistry.name(LegacyMeteredClass.class.getCanonicalName(), "overloaded-counted"));
		final Counter overloaded_param = metricRegistry.getCounters().get(
				MetricRegistry.name(LegacyMeteredClass.class.getCanonicalName(), "overloaded-counted-param"));

		assertEquals(0, overloaded.getCount());
		assertEquals(0, overloaded_param.getCount());

		meteredClass.overloadedCountedMethod(new Runnable() {
			@Override
			public void run() {
				assertEquals(1, overloaded.getCount());
				assertEquals(0, overloaded_param.getCount());
			}
		});

		assertEquals(0, overloaded.getCount());
		assertEquals(0, overloaded_param.getCount());

		meteredClass.overloadedCountedMethod(1, new Runnable() {
			@Override
			public void run() {
				assertEquals(0, overloaded.getCount());
				assertEquals(1, overloaded_param.getCount());
			}
		});

		assertEquals(0, overloaded.getCount());
		assertEquals(0, overloaded_param.getCount());
	}

	@Test
	public void scopeTest() {
		Counter publicScopeMethodCounter = forLegacyCountedMethod(metricRegistry, LegacyMeteredClass.class, "publicScopeMethod");
		assertEquals(0, publicScopeMethodCounter.getCount());
		meteredClass.publicScopeMethod();
		assertEquals(1, publicScopeMethodCounter.getCount());

		Counter packageScopeMethodCounter = forLegacyCountedMethod(metricRegistry, LegacyMeteredClass.class, "packageScopeMethod");
		assertEquals(0, packageScopeMethodCounter.getCount());
		meteredClass.packageScopeMethod();
		assertEquals(1, packageScopeMethodCounter.getCount());

		Counter protectedScopeMethodCounter = forLegacyCountedMethod(metricRegistry, LegacyMeteredClass.class, "protectedScopeMethod");
		assertEquals(0, protectedScopeMethodCounter.getCount());
		meteredClass.protectedScopeMethod();
		assertEquals(1, protectedScopeMethodCounter.getCount());

		Counter privateScopeMethodCounter = forLegacyCountedMethod(metricRegistry, LegacyMeteredClass.class, "privateScopeMethod");
		assertTrue(privateScopeMethodCounter == null);
	}

	public static class LegacyMeteredClass {

		private int gaugedField = 999;

		public void setGaugedField(int value) {
			this.gaugedField = value;
		}

		@com.ryantenney.metrics.annotation.CachedGauge(timeout = 1, timeoutUnit = TimeUnit.DAYS)
		public int cachedGaugedMethod() {
			return this.gaugedField;
		}

		@Counted
		public void countedMethod(Runnable runnable) {
			runnable.run();
		}

		@Counted(monotonic = true)
		public void monotonicCountedMethod() {}

		@Counted(name = "overloaded-counted")
		public void overloadedCountedMethod(Runnable runnable) {
			runnable.run();
		}

		@Counted(name = "overloaded-counted-param")
		public void overloadedCountedMethod(int param, Runnable runnable) {
			runnable.run();
		}

		@Counted(name = "public-scope-method", monotonic = true)
		public void publicScopeMethod() {}

		@Counted(name = "package-scope-method", monotonic = true)
		void packageScopeMethod() {}

		@Counted(name = "protected-scope-method", monotonic = true)
		protected void protectedScopeMethod() {}

		@Counted(name = "private-scope-method", monotonic = true)
		private void privateScopeMethod() {}

	}

}
