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
 * An annotation requesting that a metric be injected
 * <p/>
 * Given a field like this:
 * <pre><code>
 *     \@Metric
 *     public Meter someTimer;
 * </code></pre>
 * <p/>
 * A meter for the defining class with the name {@code someTimer} will be created. It will be up to the user
 * to mark the meter. This annotation can be used on fields of type Meter, Timer, Counter, and Histogram.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Metric {

	/**
	 * The name of the metric.
	 */
	String name() default "";

	/**
	 * If {@code true}, use the given name an as absolute name. If {@code false}, use the given name
	 * relative to the annotated class.
	 */
	boolean absolute() default false;

}
