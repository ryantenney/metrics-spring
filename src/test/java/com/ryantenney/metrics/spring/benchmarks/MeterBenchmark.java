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
package com.ryantenney.metrics.spring.benchmarks;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.annotation.Metered;
import com.google.caliper.Benchmark;
import com.google.caliper.runner.CaliperMain;
import com.ryantenney.metrics.spring.config.annotation.EnableMetrics;

public class MeterBenchmark extends Benchmark {

	public static void main(String[] args) throws Exception {
		CaliperMain.main(MeterBenchmark.class, args);
	}

	private AnnotationConfigApplicationContext ctx;
	private BenchmarkTarget targetBean;

	private Meter meter;
	private BenchmarkTarget targetObj;

	@Override
	protected void setUp() throws Exception {
		ctx = new AnnotationConfigApplicationContext(BenchmarkConfig.class);
		ctx.start();
		targetBean = ctx.getBean(BenchmarkTarget.class);

		meter = new MetricRegistry().meter("foobar");
		targetObj = new BenchmarkTarget();
	}

	@Override
	protected void tearDown() throws Exception {
		ctx.stop();
	}

	public void timeBean(int reps) {
		for (int i = 0; i < reps; i++) {
			targetBean.mark();
		}
	}

	public void timeNormal(int reps) {
		for (int i = 0; i < reps; i++) {
			meter.mark();
			targetObj.mark();
		}
	}

	@Configuration
	@EnableMetrics
	public static class BenchmarkConfig {

		@Bean
		public BenchmarkTarget createTarget() {
			return new BenchmarkTarget();
		}

	}

	public static class BenchmarkTarget {
		@Metered
		public Object mark() {
			return null;
		}
	}

}
