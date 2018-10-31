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

import org.springframework.aop.framework.ProxyConfig;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheckRegistry;

public class MetricsBeanPostProcessorFactory {

	private MetricsBeanPostProcessorFactory() {}

	public static AdvisingBeanPostProcessor exceptionMetered(final MetricRegistry metricRegistry, final ProxyConfig proxyConfig) {
		return new AdvisingBeanPostProcessor(ExceptionMeteredMethodInterceptor.POINTCUT, ExceptionMeteredMethodInterceptor.adviceFactory(metricRegistry),
				proxyConfig);
	}

	public static AdvisingBeanPostProcessor metered(final MetricRegistry metricRegistry, final ProxyConfig proxyConfig) {
		return new AdvisingBeanPostProcessor(MeteredMethodInterceptor.POINTCUT, MeteredMethodInterceptor.adviceFactory(metricRegistry), proxyConfig);
	}

	public static AdvisingBeanPostProcessor timed(final MetricRegistry metricRegistry, final ProxyConfig proxyConfig) {
		return new AdvisingBeanPostProcessor(TimedMethodInterceptor.POINTCUT, TimedMethodInterceptor.adviceFactory(metricRegistry), proxyConfig);
	}

	public static AdvisingBeanPostProcessor counted(final MetricRegistry metricRegistry, final ProxyConfig proxyConfig) {
		return new AdvisingBeanPostProcessor(CountedMethodInterceptor.POINTCUT, CountedMethodInterceptor.adviceFactory(metricRegistry), proxyConfig);
	}

	public static GaugeFieldAnnotationBeanPostProcessor gaugeField(final MetricRegistry metricRegistry) {
		return new GaugeFieldAnnotationBeanPostProcessor(metricRegistry);
	}

	public static GaugeMethodAnnotationBeanPostProcessor gaugeMethod(final MetricRegistry metricRegistry) {
		return new GaugeMethodAnnotationBeanPostProcessor(metricRegistry);
	}

	public static CachedGaugeAnnotationBeanPostProcessor cachedGauge(final MetricRegistry metricRegistry) {
		return new CachedGaugeAnnotationBeanPostProcessor(metricRegistry);
	}

	public static MetricAnnotationBeanPostProcessor metric(final MetricRegistry metricRegistry) {
		return new MetricAnnotationBeanPostProcessor(metricRegistry);
	}

	public static HealthCheckBeanPostProcessor healthCheck(final HealthCheckRegistry healthRegistry) {
		return new HealthCheckBeanPostProcessor(healthRegistry);
	}

	@Deprecated
	public static AdvisingBeanPostProcessor legacyCounted(final MetricRegistry metricRegistry, final ProxyConfig proxyConfig) {
		return new AdvisingBeanPostProcessor(LegacyCountedMethodInterceptor.POINTCUT, LegacyCountedMethodInterceptor.adviceFactory(metricRegistry), proxyConfig);
	}

	@Deprecated
	public static LegacyCachedGaugeAnnotationBeanPostProcessor legacyCachedGauge(final MetricRegistry metricRegistry) {
		return new LegacyCachedGaugeAnnotationBeanPostProcessor(metricRegistry);
	}

	@Deprecated
	public static LegacyMetricAnnotationBeanPostProcessor legacyMetric(final MetricRegistry metricRegistry) {
		return new LegacyMetricAnnotationBeanPostProcessor(metricRegistry);
	}

}
