package com.ryantenney.metrics.spring;

import org.aopalliance.aop.Advice;
import org.springframework.aop.Pointcut;
import org.springframework.aop.framework.ProxyConfig;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheckRegistry;

public class MetricsBeanPostProcessorFactory {

	private MetricsBeanPostProcessorFactory() {
	}

	public static AdvisingBeanPostProcessor exceptionMetered(final MetricRegistry metricRegistry, final ProxyConfig proxyConfig) {
		final Pointcut pointcut = ExceptionMeteredMethodInterceptor.POINTCUT;
		final AdviceFactory adviceFactory = new AdviceFactory() {
			@Override
			public Advice getAdvice(Object bean, Class<?> targetClass) {
				return new ExceptionMeteredMethodInterceptor(metricRegistry, targetClass);
			}
		};
		return new AdvisingBeanPostProcessor(pointcut, adviceFactory, proxyConfig);
	}

	public static AdvisingBeanPostProcessor metered(final MetricRegistry metricRegistry, final ProxyConfig proxyConfig) {
		final Pointcut pointcut = MeteredMethodInterceptor.POINTCUT;
		final AdviceFactory adviceFactory = new AdviceFactory() {
			@Override
			public Advice getAdvice(Object bean, Class<?> targetClass) {
				return new MeteredMethodInterceptor(metricRegistry, targetClass);
			}
		};
		return new AdvisingBeanPostProcessor(pointcut, adviceFactory, proxyConfig);
	}

	public static AdvisingBeanPostProcessor timed(final MetricRegistry metricRegistry, final ProxyConfig proxyConfig) {
		final Pointcut pointcut = TimedMethodInterceptor.POINTCUT;
		final AdviceFactory adviceFactory = new AdviceFactory() {
			@Override
			public Advice getAdvice(Object bean, Class<?> targetClass) {
				return new TimedMethodInterceptor(metricRegistry, targetClass);
			}
		};
		return new AdvisingBeanPostProcessor(pointcut, adviceFactory, proxyConfig);
	}

	public static AdvisingBeanPostProcessor counted(final MetricRegistry metricRegistry, final ProxyConfig proxyConfig) {
		final Pointcut pointcut = CountedMethodInterceptor.POINTCUT;
		final AdviceFactory adviceFactory = new AdviceFactory() {
			@Override
			public Advice getAdvice(Object bean, Class<?> targetClass) {
				return new CountedMethodInterceptor(metricRegistry, targetClass);
			}
		};
		return new AdvisingBeanPostProcessor(pointcut, adviceFactory, proxyConfig);
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
