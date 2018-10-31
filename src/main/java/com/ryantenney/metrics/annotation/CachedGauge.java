/**
 * Copyright © 2012 Ryan W Tenney (ryan@10e.us)
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
package com.ryantenney.metrics.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 * An annotation for marking a method as a gauge, which caches the result for a specified time.
 *
 * <p></p>
 * Given a method like this:
 * <pre><code>
 *     {@literal @}CachedGauge(name = "queueSize", timeout = 30, timeoutUnit = TimeUnit.SECONDS)
 *     public int getQueueSize() {
 *         return queue.getSize();
 *     }
 *
 * </code></pre>
 * <p></p>
 * 
 * A gauge for the defining class with the name queueSize will be created which uses the annotated method's
 * return value as its value, and which caches the result for 30 seconds.
 */
@Deprecated
@com.codahale.metrics.annotation.CachedGauge(timeout = 0)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface CachedGauge {

	/**
	 * The name of the counter.
	 */
	String name() default "";

	/**
	 * If {@code true}, use the given name as an absolute name. If {@code false}, use the given name
	 * relative to the annotated class.
	 */
	boolean absolute() default false;

	/**
	 * The amount of time to cache the result
	 */
	long timeout();

	/**
	 * The unit of timeout
	 */
	TimeUnit timeoutUnit() default TimeUnit.MILLISECONDS;

}
