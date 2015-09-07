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

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.ryantenney.metrics.CompositeTimer;
import com.ryantenney.metrics.annotation.CompositeTimed;
import org.aopalliance.aop.Advice;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.Pointcut;
import org.springframework.aop.support.annotation.AnnotationMatchingPointcut;
import org.springframework.core.Ordered;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

import static com.ryantenney.metrics.spring.AnnotationFilter.PROXYABLE_METHODS;

public class CompositeTimedMethodInterceptor extends AbstractMetricMethodInterceptor<CompositeTimed, CompositeTimer> implements Ordered
{
    public static final Class<CompositeTimed> ANNOTATION = CompositeTimed.class;
    public static final Pointcut POINTCUT = new AnnotationMatchingPointcut(null, ANNOTATION);
    public static final ReflectionUtils.MethodFilter METHOD_FILTER = new AnnotationFilter(ANNOTATION, PROXYABLE_METHODS);

    public CompositeTimedMethodInterceptor(final MetricRegistry metricRegistry, final Class<?> targetClass)
    {
        super(metricRegistry, targetClass, ANNOTATION, METHOD_FILTER);
    }

    @Override
    protected Object invoke(final MethodInvocation invocation, final CompositeTimer metric, final CompositeTimed annotation) throws Throwable
    {
        final Timer.Context timerCtx = metric.getTotalTimer().time();
        boolean success = true;
        final Object result;
        try
        {
            result = invocation.proceed();
        }
        catch (Throwable t)
        {
            success = false;
            throw  t;
        }
        finally
        {
            final long elapsed = timerCtx.stop();
            updateTimer(success ? metric.getSuccessTimer() : metric.getFailureTimer(), elapsed);
        }

        return result;
    }

    private void updateTimer(final Timer timer, final long elapsed)
    {
        timer.update(elapsed, TimeUnit.NANOSECONDS);
    }

    @Override
    protected CompositeTimer buildMetric(MetricRegistry metricRegistry, String metricName, CompositeTimed annotation)
    {
        final Timer totalTimer = metricRegistry.timer(metricName);
        final Timer successTimer = metricRegistry.timer(metricName + annotation.successSuffix());
        final Timer failureTimer = metricRegistry.timer(metricName + annotation.failedSuffix());
        return new CompositeTimer(totalTimer, successTimer, failureTimer);
    }

    @Override
    protected String buildMetricName(Class<?> targetClass, Method method, CompositeTimed annotation)
    {
        return Util.chooseName(annotation.name(), annotation.absolute(), targetClass, method);
    }

    @Override
    public int getOrder()
    {
        return HIGHEST_PRECEDENCE;
    }

    static AdviceFactory adviceFactory(final MetricRegistry metricRegistry) {
        return new AdviceFactory() {
            @Override
            public Advice getAdvice(Object bean, Class<?> targetClass) {
                return new CompositeTimedMethodInterceptor(metricRegistry, targetClass);
            }
        };
    }
}
