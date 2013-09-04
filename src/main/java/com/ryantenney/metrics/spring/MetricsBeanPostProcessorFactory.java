package com.ryantenney.metrics.spring;

import org.springframework.aop.framework.ProxyConfig;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheckRegistry;

public class MetricsBeanPostProcessorFactory {

	private MetricsBeanPostProcessorFactory() {
	}

	public static AdvisingBeanPostProcessor exceptionMetered(final MetricRegistry metricRegistry, final ProxyConfig proxyConfig) {
		return new AdvisingBeanPostProcessor(ExceptionMeteredMethodInterceptor.POINTCUT,
				ExceptionMeteredMethodInterceptor.adviceFactory(metricRegistry), proxyConfig);
	}

	public static AdvisingBeanPostProcessor metered(final MetricRegistry metricRegistry, final ProxyConfig proxyConfig) {
		return new AdvisingBeanPostProcessor(MeteredMethodInterceptor.POINTCUT,
				MeteredMethodInterceptor.adviceFactory(metricRegistry), proxyConfig);
	}

	public static AdvisingBeanPostProcessor timed(final MetricRegistry metricRegistry, final ProxyConfig proxyConfig) {
		return new AdvisingBeanPostProcessor(TimedMethodInterceptor.POINTCUT,
				TimedMethodInterceptor.adviceFactory(metricRegistry), proxyConfig);
	}

	public static AdvisingBeanPostProcessor counted(final MetricRegistry metricRegistry, final ProxyConfig proxyConfig) {
		return new AdvisingBeanPostProcessor(CountedMethodInterceptor.POINTCUT,
				CountedMethodInterceptor.adviceFactory(metricRegistry), proxyConfig);
	}

	public static AdvisingBeanPostProcessor histogram(final MetricRegistry metricRegistry, final ProxyConfig proxyConfig) {
		return new AdvisingBeanPostProcessor(HistogramMethodInterceptor.POINTCUT,
				HistogramMethodInterceptor.adviceFactory(metricRegistry), proxyConfig);
	}

	public static GaugeAnnotationBeanPostProcessor gauge(final MetricRegistry metricRegistry) {
		return new GaugeAnnotationBeanPostProcessor(metricRegistry);
	}

	public static InjectMetricAnnotationBeanPostProcessor injectMetric(final MetricRegistry metricRegistry) {
		return new InjectMetricAnnotationBeanPostProcessor(metricRegistry);
	}

	public static HealthCheckBeanPostProcessor healthCheck(final HealthCheckRegistry healthRegistry) {
		return new HealthCheckBeanPostProcessor(healthRegistry);
	}

}
