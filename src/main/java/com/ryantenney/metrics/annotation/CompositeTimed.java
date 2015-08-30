package com.ryantenney.metrics.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation for marking a method of an annotated object as composite timed.
 * <p/>
 * Given a method like this:
 * <pre><code>
 *     {@literal @}CompositeTimed(name = "fancyName")
 *     public String fancyName(String name) {
 *         return "Sir Captain " + name;
 *     }
 * </code></pre>
 * <p/>
 * Three timers for the defining class with the name {@code fancyName} will be created:
 * <ul>
 *     <li>One to time all the method calls</li>
 *     <li>One to time all successful method calls</li>
 *     <li>One to time all failing method calls</li>
 * </ul>
 * and each time the
 * {@code #fancyName(String)} method is invoked, the method's execution will be timed.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface CompositeTimed {
    /**
     * The name of the timer.
     */
    String name() default "";

    /**
     * The suffix for the successful timer.
     */
    String successSuffix() default ".success";

    /**
     * The suffix for the failing timer.
     */
    String failedSuffix() default ".failed";

    /**
     * If {@code true}, use the given name as an absolute name. If {@code false}, use the given name
     * relative to the annotated class.
     */
    boolean absolute() default false;
}
