package com.ryantenney.metrics.spring;

import com.yammer.metrics.annotation.Gauge;
import com.yammer.metrics.core.MetricName;
import com.yammer.metrics.core.MetricsRegistry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.ReflectionUtils.FieldCallback;
import org.springframework.util.ReflectionUtils.MethodCallback;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class GaugeAnnotationBeanPostProcessor implements BeanPostProcessor, Ordered {

	private static final Logger log = LoggerFactory.getLogger(GaugeAnnotationBeanPostProcessor.class);

	private static final AnnotationFilter filter = new AnnotationFilter(Gauge.class);

	private final MetricsRegistry metrics;
	private final String scope;

	public GaugeAnnotationBeanPostProcessor(final MetricsRegistry metrics, final String scope) {
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
			public void doWith(final Field field) throws IllegalArgumentException, IllegalAccessException {
				ReflectionUtils.makeAccessible(field);

				final Gauge annotation = field.getAnnotation(Gauge.class);
				final MetricName metricName = Util.forGauge(targetClass, field, annotation, scope);

				metrics.newGauge(metricName, new com.yammer.metrics.core.Gauge<Object>() {
					@Override
					public Object getValue() {
						Object value = ReflectionUtils.getField(field, bean);
						if (value instanceof com.yammer.metrics.core.Gauge) {
							value = ((com.yammer.metrics.core.Gauge<?>) value).getValue();
						}
						return value;
					}
				});

				log.debug("Created gauge {} for field {}.{}", new Object[] { metricName, targetClass.getCanonicalName(), field.getName() });
			}
		}, filter);

		ReflectionUtils.doWithMethods(targetClass, new MethodCallback() {
			@Override
			public void doWith(final Method method) throws IllegalArgumentException, IllegalAccessException {
				if (method.getParameterTypes().length > 0) {
					throw new IllegalStateException("Method " + method.getName() + " is annotated with @Gauge but requires parameters.");
				}

				final Gauge annotation = method.getAnnotation(Gauge.class);
				final MetricName metricName = Util.forGauge(targetClass, method, annotation, scope);

				metrics.newGauge(metricName, new com.yammer.metrics.core.Gauge<Object>() {
					@Override
					public Object getValue() {
						return ReflectionUtils.invokeMethod(method, bean);
					}
				});

				log.debug("Created gauge {} for method {}.{}", new Object[] { metricName, targetClass.getCanonicalName(), method.getName() });
			}
		}, filter);

		return bean;
	}

	@Override
	public int getOrder() {
		return LOWEST_PRECEDENCE;
	}

}
