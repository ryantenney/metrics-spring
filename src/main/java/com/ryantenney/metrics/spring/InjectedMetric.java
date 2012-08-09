package com.ryantenney.metrics.spring;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 * An annotation requesting that a metric be injected
 * <p/>
 * Given a field like this:
 * <pre><code>
 *     \@InjectedMetric
 *     public Meter someTimer;
 * </code></pre>
 * <p/>
 * A meter for the defining class with the name {@code someTimer} will be created. It will be up to the user to mark the meter. This annotation can be used on fields of type Meter, Timer, Counter, and Histogram.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface InjectedMetric {

	/**
	 * The group of the metric.
	 */
	String group() default "";

	/**
	 * The type of the metric.
	 */
	String type() default "";

	/**
	 * The name of the metric.
	 */
	String name() default "";

	/**
	 * The name of the type of events the meter is measuring.
	 * Applies to Meter
	 */
	String eventType() default "calls";

	/**
	 * The time unit of the meter's rate.
	 * Applies to Meter, Timer
	 */
	TimeUnit rateUnit() default TimeUnit.SECONDS;

	/**
	 * The time unit of the timer's duration.
	 * Applies to Timer
	 */
	TimeUnit durationUnit() default TimeUnit.MILLISECONDS;

	/**
	 * The type of sampling that should be performed.
	 * Applies to Histogram
	 */
	boolean biased() default false;

}
