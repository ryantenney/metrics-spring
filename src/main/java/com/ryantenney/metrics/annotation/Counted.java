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

/**
 * An annotation for marking a method of an annotated object as counted.
 *
 * <p></p>
 * Given a method like this:
 * <pre><code>
 *     {@literal @}Counted(name = "fancyName")
 *     public String fancyName(String name) {
 *         return "Sir Captain " + name;
 *     }
 * </code></pre>
 * <p></p>
 * A counter for the defining class with the name {@code fancyName} will be created and each time the
 * {@code #fancyName(String)} method is invoked, the counter will be marked.
 */
@Deprecated
@com.codahale.metrics.annotation.Counted
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Counted {

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
	 * If {@code false} (default), counter is decremented when the annotated
	 * method returns, counts current invocations of the annotated method.
	 * If {@code true}, counter increases monotonically, counts total number
	 * of invocations of the annotated method.
	 */
	boolean monotonic() default false;

}
