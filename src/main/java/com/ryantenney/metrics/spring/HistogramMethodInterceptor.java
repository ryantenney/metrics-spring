/*
 * Copyright 2012 Ryan W Tenney (http://ryan.10e.us)
 *            and Martello Technologies (http://martellotech.com)
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

import java.lang.reflect.Method;

import org.aopalliance.aop.Advice;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.Pointcut;
import org.springframework.aop.support.annotation.AnnotationMatchingPointcut;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.core.MethodParameter;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.expression.AccessException;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.EvaluationException;
import org.springframework.expression.Expression;
import org.springframework.expression.ParserContext;
import org.springframework.expression.PropertyAccessor;
import org.springframework.expression.TypedValue;
import org.springframework.expression.common.TemplateParserContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.util.ReflectionUtils.MethodFilter;

import com.codahale.metrics.MetricRegistry;
import com.ryantenney.metrics.annotation.Histogram;
import com.ryantenney.metrics.spring.HistogramMethodInterceptor.HistogramExpression;

class HistogramMethodInterceptor extends AbstractMetricMethodInterceptor<Histogram, HistogramExpression> {

	public static final Class<Histogram> ANNOTATION = Histogram.class;
	public static final Pointcut POINTCUT = new AnnotationMatchingPointcut(null, ANNOTATION);
	public static final MethodFilter METHOD_FILTER = new AnnotationFilter(ANNOTATION);

	public HistogramMethodInterceptor(final MetricRegistry metricRegistry, final Class<?> targetClass) {
		super(metricRegistry, targetClass, ANNOTATION, METHOD_FILTER);
	}

	@Override
	protected Object invoke(MethodInvocation invocation, HistogramExpression histogram) throws Throwable {
		try {
			SpelExpressionParser el = new SpelExpressionParser();
			ParserContext parserCtx = new TemplateParserContext();
			Expression expression = el.parseExpression(histogram.getExpression(), parserCtx);
			StandardEvaluationContext evalCtx = new StandardEvaluationContext();
			evalCtx.addPropertyAccessor(new NamedParameterPropertyAccessor(invocation.getMethod(), invocation.getArguments()));
			Long value = expression.getValue(evalCtx, Long.class);
			if (value != null) {
				histogram.getHistogram().update(value);
			}
		}
		catch (EvaluationException e) {
			LOG.error("Error evaluating expression", e);
		}
		return invocation.proceed();
	}

	@Override
	protected HistogramExpression buildMetric(MetricRegistry metricRegistry, String metricName, Histogram annotation) {
		return new HistogramExpression(metricRegistry.histogram(metricName), annotation.value());
	}
	
	@Override
	protected String buildMetricName(Class<?> targetClass, Method method, Histogram annotation) {
		return Util.forHistogramMethod(targetClass, method, annotation);
	}

	static AdviceFactory adviceFactory(final MetricRegistry metricRegistry) {
		return new AdviceFactory() {
			@Override
			public Advice getAdvice(Object bean, Class<?> targetClass) {
				return new HistogramMethodInterceptor(metricRegistry, targetClass);
			}
		};
	}

	class NamedParameterPropertyAccessor implements PropertyAccessor {

		private final Method method;
		private final Object[] params;

		public NamedParameterPropertyAccessor(Method method, Object[] params) {
			this.method = method;
			this.params = params;
		}

		@Override
		public void write(EvaluationContext context, Object target, String name, Object newValue) throws AccessException {}

		@Override
		public TypedValue read(EvaluationContext context, Object target, String name) throws AccessException {
			if (target == null) {
				int len = method.getParameterTypes().length;
				for (int i = 0; i < len; i++) {
					MethodParameter param = new MethodParameter(method, i);
					LocalVariableTableParameterNameDiscoverer parameterNameDiscoverer = new LocalVariableTableParameterNameDiscoverer();
					param.initParameterNameDiscovery(parameterNameDiscoverer);
					LOG.info("Method: {}, Name: {}", method, param.getParameterName());
					if (name.equals(param.getParameterName())) {
						LOG.info("param: {}, index: {}, td: {}", params[i], i, new TypeDescriptor(param));
						return new TypedValue(params[i], new TypeDescriptor(param));
					}
				}
			}
			return null;
		}

		@Override
		public Class<?>[] getSpecificTargetClasses() {
			return null;
		}

		@Override
		public boolean canWrite(EvaluationContext context, Object target, String name) throws AccessException {
			return false;
		}

		@Override
		public boolean canRead(EvaluationContext context, Object target, String name) throws AccessException {
			return true;
		}

	}

	static class HistogramExpression {

		private final com.codahale.metrics.Histogram histogram;
		private final String expression;

		public HistogramExpression(final com.codahale.metrics.Histogram histogram, final String expression) {
			this.histogram = histogram;
			this.expression = expression;
		}

		public com.codahale.metrics.Histogram getHistogram() {
			return histogram;
		}

		public String getExpression() {
			return expression;
		}

	}

}
