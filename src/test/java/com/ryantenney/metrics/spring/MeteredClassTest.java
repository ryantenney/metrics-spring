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

import static com.ryantenney.metrics.spring.TestUtil.forCountedMethod;
import static com.ryantenney.metrics.spring.TestUtil.forExceptionMeteredMethod;
import static com.ryantenney.metrics.spring.TestUtil.forGaugeField;
import static com.ryantenney.metrics.spring.TestUtil.forGaugeMethod;
import static com.ryantenney.metrics.spring.TestUtil.forMeteredMethod;
import static com.ryantenney.metrics.spring.TestUtil.forTimedMethod;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.RatioGauge;
import com.codahale.metrics.Timer;
import com.codahale.metrics.annotation.ExceptionMetered;
import com.codahale.metrics.annotation.Metered;
import com.codahale.metrics.annotation.Timed;
import com.ryantenney.metrics.annotation.Counted;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:metered-class.xml")
public class MeteredClassTest {

	@Autowired
	MeteredClass meteredClass;

	MetricRegistry metricRegistry;

	@Autowired
	public void setMetricRegistry(MetricRegistry metricRegistry) {
		this.metricRegistry = metricRegistry;
		this.metricRegistry.addListener(new LoggingMetricRegistryListener());
	}

	@Test
	public void gauges() {
		Gauge<?> gaugedField = forGaugeField(metricRegistry, MeteredClass.class, "gaugedField");
		Gauge<?> gaugedMethod = forGaugeMethod(metricRegistry, MeteredClass.class, "gaugedMethod");
		Gauge<?> gaugedGaugeField = forGaugeField(metricRegistry, MeteredClass.class, "gaugedGaugeField");

		assertEquals(999, gaugedField.getValue());
		assertEquals(999, gaugedMethod.getValue());

		meteredClass.setGaugedField(1000);

		assertEquals(1000, gaugedField.getValue());
		assertEquals(1000, gaugedMethod.getValue());

		assertEquals(0.5, gaugedGaugeField.getValue());
	}

	@Test
	public void timedMethod() throws Throwable {
		Timer timedMethod = forTimedMethod(metricRegistry, MeteredClass.class, "timedMethod");

		assertEquals(0, timedMethod.getCount());

		meteredClass.timedMethod(false);
		assertEquals(1, timedMethod.getCount());

		// getCount increments even when the method throws an exception
		try {
			meteredClass.timedMethod(true);
			fail();
		}
		catch (Throwable e) {
			assertTrue(e instanceof BogusException);
		}
		assertEquals(2, timedMethod.getCount());
	}

	@Test
	public void meteredMethod() throws Throwable {
		Meter meteredMethod = forMeteredMethod(metricRegistry, MeteredClass.class, "meteredMethod");

		assertEquals(0, meteredMethod.getCount());

		meteredClass.meteredMethod();
		assertEquals(1, meteredMethod.getCount());
	}

	@Test
	public void exceptionMeteredMethod() throws Throwable {
		Meter exceptionMeteredMethod = forExceptionMeteredMethod(metricRegistry, MeteredClass.class, "exceptionMeteredMethod");

		assertEquals(0, exceptionMeteredMethod.getCount());

		// doesn't throw an exception
		meteredClass.exceptionMeteredMethod(null);
		assertEquals(0, exceptionMeteredMethod.getCount());

		// throws the wrong exception
		try {
			meteredClass.exceptionMeteredMethod(RuntimeException.class);
			fail();
		}
		catch (Throwable t) {
			assertTrue(t instanceof RuntimeException);
		}
		assertEquals(0, exceptionMeteredMethod.getCount());

		// throws the right exception
		try {
			meteredClass.exceptionMeteredMethod(BogusException.class);
			fail();
		}
		catch (Throwable t) {
			assertTrue(t instanceof BogusException);
		}
		assertEquals(1, exceptionMeteredMethod.getCount());
	}

	@Test
	public void countedMethod() throws Throwable {
		final Counter countedMethod = forCountedMethod(metricRegistry, MeteredClass.class, "countedMethod");

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
		final Counter countedMethod = forCountedMethod(metricRegistry, MeteredClass.class, "monotonicCountedMethod");

		assertEquals(0, countedMethod.getCount());

		meteredClass.monotonicCountedMethod();
		assertEquals(1, countedMethod.getCount());

		meteredClass.monotonicCountedMethod();
		assertEquals(2, countedMethod.getCount());
	}

	@Test
	public void quadruplyMeteredMethod() throws Throwable {
		Timer quadruple_Timed = forTimedMethod(metricRegistry, MeteredClass.class, "quadruplyMeteredMethod");
		Meter quadruple_Metered = forMeteredMethod(metricRegistry, MeteredClass.class, "quadruplyMeteredMethod");
		Meter quadruple_ExceptionMetered = forExceptionMeteredMethod(metricRegistry, MeteredClass.class, "quadruplyMeteredMethod");
		final Counter quadruple_Counted = forCountedMethod(metricRegistry, MeteredClass.class, "quadruplyMeteredMethod");

		assertEquals(0, quadruple_Metered.getCount());
		assertEquals(0, quadruple_Timed.getCount());
		assertEquals(0, quadruple_ExceptionMetered.getCount());
		assertEquals(0, quadruple_Counted.getCount());

		// doesn't throw an exception
		meteredClass.quadruplyMeteredMethod(new Runnable() {
			@Override
			public void run() {
				assertEquals(1, quadruple_Counted.getCount());
			}
		});

		assertEquals(1, quadruple_Metered.getCount());
		assertEquals(1, quadruple_Timed.getCount());
		assertEquals(0, quadruple_ExceptionMetered.getCount());
		assertEquals(0, quadruple_Counted.getCount());

		// throws an exception
		try {
			meteredClass.quadruplyMeteredMethod(new Runnable() {
				@Override
				public void run() {
					assertEquals(1, quadruple_Counted.getCount());
					throw new BogusException();
				}
			});
			fail();
		}
		catch (Throwable t) {
			assertTrue(t instanceof BogusException);
		}
		assertEquals(2, quadruple_Metered.getCount());
		assertEquals(2, quadruple_Timed.getCount());
		assertEquals(1, quadruple_ExceptionMetered.getCount());
		assertEquals(0, quadruple_Counted.getCount());
	}

	@Test
	public void overloadedTimedMethod() {
		Timer overloaded = metricRegistry.getTimers().get(MetricRegistry.name(MeteredClass.class.getCanonicalName(), "overloaded-timed"));
		Timer overloaded_param = metricRegistry.getTimers().get(MetricRegistry.name(MeteredClass.class.getCanonicalName(), "overloaded-timed-param"));

		assertEquals(0, overloaded.getCount());
		assertEquals(0, overloaded_param.getCount());

		meteredClass.overloadedTimedMethod();

		assertEquals(1, overloaded.getCount());
		assertEquals(0, overloaded_param.getCount());

		meteredClass.overloadedTimedMethod(1);

		assertEquals(1, overloaded.getCount());
		assertEquals(1, overloaded_param.getCount());

		meteredClass.overloadedTimedMethod(1);

		assertEquals(1, overloaded.getCount());
		assertEquals(2, overloaded_param.getCount());
	}

	@Test
	public void overloadedMeteredMethod() {
		Meter overloaded = metricRegistry.getMeters().get(MetricRegistry.name(MeteredClass.class.getCanonicalName(), "overloaded-metered"));
		Meter overloaded_param = metricRegistry.getMeters().get(MetricRegistry.name(MeteredClass.class.getCanonicalName(), "overloaded-metered-param"));

		assertEquals(0, overloaded.getCount());
		assertEquals(0, overloaded_param.getCount());

		meteredClass.overloadedMeteredMethod();

		assertEquals(1, overloaded.getCount());
		assertEquals(0, overloaded_param.getCount());

		meteredClass.overloadedMeteredMethod(1);

		assertEquals(1, overloaded.getCount());
		assertEquals(1, overloaded_param.getCount());

		meteredClass.overloadedMeteredMethod(1);

		assertEquals(1, overloaded.getCount());
		assertEquals(2, overloaded_param.getCount());
	}

	@Test
	public void overloadedCountedMethod() {
		final Counter overloaded = metricRegistry.getCounters().get(MetricRegistry.name(MeteredClass.class.getCanonicalName(), "overloaded-counted"));
		final Counter overloaded_param = metricRegistry.getCounters().get(MetricRegistry.name(MeteredClass.class.getCanonicalName(), "overloaded-counted-param"));

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
	public void overloadedExceptionMeteredMethod() throws Throwable {
		Meter overloaded = metricRegistry.getMeters().get(MetricRegistry.name(MeteredClass.class.getCanonicalName(), "overloaded-exception-metered"));
		Meter overloaded_param = metricRegistry.getMeters().get(
				MetricRegistry.name(MeteredClass.class.getCanonicalName(), "overloaded-exception-metered-param"));

		assertEquals(0, overloaded.getCount());
		assertEquals(0, overloaded_param.getCount());

		// doesn't throw an exception
		meteredClass.overloadedExceptionMeteredMethod(null);
		assertEquals(0, overloaded.getCount());
		assertEquals(0, overloaded_param.getCount());

		// throws the wrong exception
		try {
			meteredClass.overloadedExceptionMeteredMethod(RuntimeException.class);
			fail();
		}
		catch (Throwable t) {
			assertTrue(t instanceof RuntimeException);
		}
		assertEquals(0, overloaded.getCount());
		assertEquals(0, overloaded_param.getCount());

		// throws the right exception
		try {
			meteredClass.overloadedExceptionMeteredMethod(BogusException.class);
			fail();
		}
		catch (Throwable t) {
			assertTrue(t instanceof BogusException);
		}
		assertEquals(1, overloaded.getCount());
		assertEquals(0, overloaded_param.getCount());

		// doesn't throw an exception
		meteredClass.overloadedExceptionMeteredMethod(null, 1);
		assertEquals(1, overloaded.getCount());
		assertEquals(0, overloaded_param.getCount());

		// throws the wrong exception
		try {
			meteredClass.overloadedExceptionMeteredMethod(RuntimeException.class, 1);
			fail();
		}
		catch (Throwable t) {
			assertTrue(t instanceof RuntimeException);
		}
		assertEquals(1, overloaded.getCount());
		assertEquals(0, overloaded_param.getCount());

		// throws the right exception
		try {
			meteredClass.overloadedExceptionMeteredMethod(BogusException.class, 1);
			fail();
		}
		catch (Throwable t) {
			assertTrue(t instanceof BogusException);
		}
		assertEquals(1, overloaded.getCount());
		assertEquals(1, overloaded_param.getCount());
	}

	public static class MeteredClass {

		@com.codahale.metrics.annotation.Gauge
		private int gaugedField = 999;

		@com.codahale.metrics.annotation.Gauge
		private RatioGauge gaugedGaugeField = new RatioGauge() {
			@Override
			protected Ratio getRatio() {
				return Ratio.of(1, 2);
			}
		};

		@com.codahale.metrics.annotation.Gauge
		public int gaugedMethod() {
			return this.gaugedField;
		}

		public void setGaugedField(int value) {
			this.gaugedField = value;
		}

		@Timed
		public void timedMethod(boolean doThrow) throws Throwable {
			if (doThrow) {
				throw new BogusException();
			}
		}

		@Metered
		public void meteredMethod() {}

		@Counted
		public void countedMethod(Runnable runnable) {
			runnable.run();
		}

		@Counted(monotonic = true)
		public void monotonicCountedMethod() {}

		@ExceptionMetered(cause = BogusException.class)
		public <T extends Throwable> void exceptionMeteredMethod(Class<T> clazz) throws Throwable {
			if (clazz != null) {
				throw clazz.newInstance();
			}
		}

		@Timed(name = "quadruplyMeteredMethod-timed")
		@Metered(name = "quadruplyMeteredMethod-metered")
		@Counted(name = "quadruplyMeteredMethod-counted")
		@ExceptionMetered(name = "quadruplyMeteredMethod-exceptionMetered", cause = BogusException.class)
		public void quadruplyMeteredMethod(Runnable runnable) throws Throwable {
			runnable.run();
		}

		@Timed(name = "overloaded-timed")
		public void overloadedTimedMethod() {}

		@Timed(name = "overloaded-timed-param")
		public void overloadedTimedMethod(int param) {}

		@Metered(name = "overloaded-metered")
		public void overloadedMeteredMethod() {}

		@Metered(name = "overloaded-metered-param")
		public void overloadedMeteredMethod(int param) {}

		@Counted(name = "overloaded-counted")
		public void overloadedCountedMethod(Runnable runnable) {
			runnable.run();
		}

		@Counted(name = "overloaded-counted-param")
		public void overloadedCountedMethod(int param, Runnable runnable) {
			runnable.run();
		}

		@ExceptionMetered(name = "overloaded-exception-metered", cause = BogusException.class)
		public <T extends Throwable> void overloadedExceptionMeteredMethod(Class<T> clazz) throws Throwable {
			if (clazz != null) {
				throw clazz.newInstance();
			}
		}

		@ExceptionMetered(name = "overloaded-exception-metered-param", cause = BogusException.class)
		public <T extends Throwable> void overloadedExceptionMeteredMethod(Class<T> clazz, int param) throws Throwable {
			if (clazz != null) {
				throw clazz.newInstance();
			}
		}

	}

	@SuppressWarnings("serial")
	public static class BogusException extends RuntimeException {}

}
