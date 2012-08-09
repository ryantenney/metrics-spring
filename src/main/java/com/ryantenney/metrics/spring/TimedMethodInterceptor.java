package com.ryantenney.metrics.spring;

import com.yammer.metrics.annotation.Timed;
import com.yammer.metrics.core.*;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.ReflectionUtils.MethodCallback;
import org.springframework.util.ReflectionUtils.MethodFilter;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

class TimedMethodInterceptor implements MethodInterceptor, MethodCallback, Ordered {

	private static final Logger log = LoggerFactory.getLogger(TimedMethodInterceptor.class);

	private static final MethodFilter filter = new AnnotationFilter(Timed.class);

	private final MetricsRegistry metrics;
	private final Class<?> targetClass;
	private final Map<String, Timer> timers;
	private final String scope;

	public TimedMethodInterceptor(final MetricsRegistry metrics, final Class<?> targetClass, final String scope) {
		this.metrics = metrics;
		this.targetClass = targetClass;
		this.timers = new HashMap<String, Timer>();
		this.scope = scope;

		log.debug("Creating method interceptor for class {}", targetClass.getCanonicalName());
		log.debug("Scanning for @Timed annotated methods");

		ReflectionUtils.doWithMethods(targetClass, this, filter);
	}

	@Override
	public Object invoke(MethodInvocation invocation) throws Throwable {
		final Timer timer = timers.get(invocation.getMethod().getName());
		final TimerContext timerCtx = timer != null ? timer.time() : null;
		try {
			return invocation.proceed();
		} finally {
			if (timerCtx != null) {
				timerCtx.stop();
			}
		}
	}

	@Override
	public void doWith(Method method) throws IllegalArgumentException, IllegalAccessException {
		final Timed annotation = method.getAnnotation(Timed.class);
		final MetricName metricName = Util.forTimedMethod(targetClass, method, annotation, scope);
		final Timer timer = metrics.newTimer(metricName, annotation.durationUnit(), annotation.rateUnit());

		timers.put(method.getName(), timer);

		log.debug("Created metric {} for method {}", metricName, method.getName());
	}

	@Override
	public int getOrder() {
		return HIGHEST_PRECEDENCE;
	}

}
