package com.ryantenney.metrics.spring;

import org.aopalliance.aop.Advice;

interface AdviceFactory {

	Advice getAdvice(Object bean, Class<?> targetClass);

}
