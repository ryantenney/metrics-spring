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
package com.ryantenney.metrics.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation allows parameter values to be injected into metric names.
 * 
 * @Timed(absolute=true, name="testmetric.{param}")
 * public void timedMethod(@MetricParam(value = "param") int value) {} 
 * 
 * Called with value=12, registers a metric with the name "testmetric.12".
 * 
 * Applies only to parameters of methods annotated with:
 * {@link Timed @Timed}, {@link Counted @Counted}, {@link Metered @Metered}, {@link ExceptionMetered @ExceptionMetered}
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.PARAMETER })
public @interface MetricParam {

    String value() default "";

    /**
     * Marks this as a collection, such that instead of the value, the number of elements 
     * will be substituted into the metric name inside parentheses, e.g. (12), if the parameter is of Collection<?> type.
     *
     * @return
     */
    boolean collection() default false;
}
