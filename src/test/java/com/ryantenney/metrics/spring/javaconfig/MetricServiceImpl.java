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
package com.ryantenney.metrics.spring.javaconfig;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import com.codahale.metrics.annotation.*;

public class MetricServiceImpl implements MetricService {

	@Gauge
	private int gaugeValue = 9999;

	@Override
	public int getGaugeValue() {
		return gaugeValue;
	}

	@Metered
	@Override
	public int meteredMethod() {
		return 9999;
	}

	@Override
	@com.ryantenney.metrics.annotation.CachedGauge(timeout = 5, timeoutUnit = TimeUnit.SECONDS)
	public int cachedGaugeMethod() {
		return 9999;
	}

	@Override
	@Gauge
	public int gaugedMethod() {
		return 9999;
	}

	@Override
	@com.ryantenney.metrics.annotation.Counted(monotonic = true)
	public int countedMethod() {
		return 9999;
	}

	@Timed
	@Override
	public void timedMethod() {
		int i = 1000;
		while (i-- > 0) {
			double d1 = new Random().nextDouble();
			double d2 = new Random().nextDouble();
			double ret = d1 * d2;
		}
	}
}
