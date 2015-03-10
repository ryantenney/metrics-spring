package com.ryantenney.metrics.annotation;

import java.lang.annotation.*;

/**
 * An annotation for marking a method of an annotated object as timed.
 * <p/>
 * Given a method like this:
 * <pre><code>
 *     {@literal @}Timed(name = "fancyName")
 *     public String fancyName(String name) {
 *         return "Sir Captain " + name;
 *     }
 * </code></pre>
 * <p/>
 * A timer for the defining class with the name {@code fancyName} will be created and each time the
 * {@code #fancyName(String)} method is invoked, the method's execution will be timed.
 */
@com.codahale.metrics.annotation.Timed
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD })
public @interface Timed {
    /**
     * @return The name of the timer.
     */
    String name() default "";

    /**
     * @return If {@code true}, use the given name as an absolute name. If {@code false}, use the given name
     * relative to the annotated class. When annotating a class, this must be {@code false}.
     */
    boolean absolute() default false;
}
