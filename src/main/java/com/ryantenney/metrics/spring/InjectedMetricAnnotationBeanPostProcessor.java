package com.ryantenney.metrics.spring;

import com.yammer.metrics.core.Counter;
import com.yammer.metrics.core.Histogram;
import com.yammer.metrics.core.Meter;
import com.yammer.metrics.core.Metric;
import com.yammer.metrics.core.MetricName;
import com.yammer.metrics.core.MetricsRegistry;
import com.yammer.metrics.core.Timer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.ReflectionUtils.FieldCallback;
import java.lang.reflect.Field;

public class InjectedMetricAnnotationBeanPostProcessor implements BeanPostProcessor, Ordered {

	private static final Logger log = LoggerFactory.getLogger(InjectedMetricAnnotationBeanPostProcessor.class);

	private static final AnnotationFilter filter = new AnnotationFilter(InjectedMetric.class);

	private final MetricsRegistry metrics;
	private final String scope;

	public InjectedMetricAnnotationBeanPostProcessor(final MetricsRegistry metrics, final String scope) {
		this.metrics = metrics;
		this.scope = scope;
	}

	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
		return bean;
	}

	@Override
	public Object postProcessAfterInitialization(final Object bean, String beanName) throws BeansException {
		final Class<?> targetClass = AopUtils.getTargetClass(bean);

		ReflectionUtils.doWithFields(targetClass, new FieldCallback() {
			@Override
			public void doWith(Field field) throws IllegalArgumentException, IllegalAccessException {
				final InjectedMetric annotation = field.getAnnotation(InjectedMetric.class);
				final MetricName metricName = Util.forInjectedMetricField(targetClass, field, annotation, scope);

				final Class<?> type = field.getType();
				Metric metric = null;
				if (Meter.class == type) {
					metric = metrics.newMeter(metricName, annotation.eventType(), annotation.rateUnit());
				} else if (Timer.class == type) {
					metric = metrics.newTimer(metricName, annotation.durationUnit(), annotation.rateUnit());
				} else if (Counter.class == type) {
					metric = metrics.newCounter(metricName);
				} else if (Histogram.class == type) {
					metric = metrics.newHistogram(metricName, annotation.biased());
				} else {
					throw new IllegalStateException("Cannot inject a metric of type " + type.getCanonicalName());
				}

				ReflectionUtils.makeAccessible(field);
				field.set(bean, metric);

				log.debug("Injected metric {} for field {}.{}", new Object[] { metricName, targetClass.getCanonicalName(), field.getName() });
			}
		}, filter);

		return bean;
	}

	@Override
	public int getOrder() {
		return LOWEST_PRECEDENCE - 2;
	}

}
