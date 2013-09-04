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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistryListener;
import com.codahale.metrics.Timer;

/**
* Created with IntelliJ IDEA.
* User: ryan
* Date: 4/2/13
* Time: 5:50 PM
* To change this template use File | Settings | File Templates.
*/
class LoggingMetricRegistryListener implements MetricRegistryListener {

	private static final Logger log = LoggerFactory.getLogger(LoggingMetricRegistryListener.class);

	@Override
	public void onGaugeAdded(String s, Gauge<?> gauge) {
		log.info("Gauge added: {}", s);
	}

	@Override
	public void onGaugeRemoved(String s) {
		log.info("Gauge removed: {}", s);
	}

	@Override
	public void onCounterAdded(String s, Counter counter) {
		log.info("Counter added: {}", s);
	}

	@Override
	public void onCounterRemoved(String s) {
		log.info("Counter removed: {}", s);
	}

	@Override
	public void onHistogramAdded(String s, Histogram histogram) {
		log.info("Histogram added: {}", s);
	}

	@Override
	public void onHistogramRemoved(String s) {
		log.info("Histogram removed: {}", s);
	}

	@Override
	public void onMeterAdded(String s, Meter meter) {
		log.info("Meter added: {}", s);
	}

	@Override
	public void onMeterRemoved(String s) {
		log.info("Meter removed: {}", s);
	}

	@Override
	public void onTimerAdded(String s, Timer timer) {
		log.info("Timer added: {}", s);
	}

	@Override
	public void onTimerRemoved(String s) {
		log.info("Timer removed: {}", s);
	}
}
