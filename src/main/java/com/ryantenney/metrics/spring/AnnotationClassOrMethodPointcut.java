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

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicBoolean;
import org.springframework.aop.MethodMatcher;
import org.springframework.aop.support.StaticMethodMatcherPointcut;
import org.springframework.aop.support.annotation.AnnotationClassFilter;
import org.springframework.aop.support.annotation.AnnotationMethodMatcher;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ReflectionUtils;

/**
 Based on spring retry (org.springframework.retry.annotation.RetryConfiguration).
 */
public class AnnotationClassOrMethodPointcut extends StaticMethodMatcherPointcut {

    private final MethodMatcher methodResolver;

    AnnotationClassOrMethodPointcut(Class<? extends Annotation> annotationType) {
        this.methodResolver = new AnnotationMethodMatcher(annotationType);
        setClassFilter(new AnnotationClassOrMethodFilter(annotationType));
    }

    @Override
    public boolean matches(Method method, Class<?> targetClass) {
        return getClassFilter().matches(targetClass) || this.methodResolver.matches(method, targetClass);
    }

    private static final class AnnotationClassOrMethodFilter extends AnnotationClassFilter {

        private final AnnotationMethodsResolver methodResolver;

        AnnotationClassOrMethodFilter(Class<? extends Annotation> annotationType) {
            super(annotationType, true);
            this.methodResolver = new AnnotationMethodsResolver(annotationType);
        }

        @Override
        public boolean matches(Class<?> clazz) {
            return super.matches(clazz) || this.methodResolver.hasAnnotatedMethods(clazz);
        }

        @Override
        public boolean equals(Object other) {

            return super.equals(other);
        }

        @Override
        public int hashCode() {

            return super.hashCode();
        }
    }

    private static class AnnotationMethodsResolver {

        private Class<? extends Annotation> annotationType;

        public AnnotationMethodsResolver(Class<? extends Annotation> annotationType) {
            this.annotationType = annotationType;
        }

        public boolean hasAnnotatedMethods(Class<?> clazz) {
            final AtomicBoolean found = new AtomicBoolean(false);
            ReflectionUtils.doWithMethods(clazz,
                    new ReflectionUtils.MethodCallback() {
                        @Override
                        public void doWith(Method method) throws IllegalArgumentException,
                                IllegalAccessException {
                            if (found.get()) {
                                return;
                            }
                            Annotation annotation = AnnotationUtils.findAnnotation(method,
                                    annotationType);
                            if (annotation != null) { found.set(true); }
                        }
                    });
            return found.get();
        }

    }

}
