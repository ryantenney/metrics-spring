package com.ryantenney.metrics.spring.config.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Import;

/**
 * Add this annotation to an {@code @Configuration} class to have the Metrics
 * configuration defined in {@link MetricsConfigurationSupport} imported.
 * 
 * @see MetricsConfigurer
 * @see MetricsConfigurerAdapter
 * 
 * @author Ryan Tenney
 * @since 2.2
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(DelegatingMetricsConfiguration.class)
public @interface EnableMetrics {

	/**
	 * The scope of the {@code Metric}
	 */
	String scope() default "";

	/**
	 * Set whether the proxy should be exposed by the AOP framework as a
	 * ThreadLocal for retrieval via the AopContext class. This is useful
	 * if an advised object needs to call another advised method on itself.
	 * (If it uses <code>this</code>, the invocation will not be advised).
	 * <p>Default is "false", in order to avoid unnecessary extra interception.
	 * This means that no guarantees are provided that AopContext access will
	 * work consistently within any method of the advised object.
	 */
	boolean exposeProxy() default false;

	/**
	 * Set whether to proxy the target class directly, instead of just proxying
	 * specific interfaces. Default is "false".
	 * <p>Set this to "true" to force proxying for the TargetSource's exposed
	 * target class. If that target class is an interface, a JDK proxy will be
	 * created for the given interface. If that target class is any other class,
	 * a CGLIB proxy will be created for the given class.
	 * <p>Note: Depending on the configuration of the concrete proxy factory,
	 * the proxy-target-class behavior will also be applied if no interfaces
	 * have been specified (and no interface autodetection is activated).
	 * @see org.springframework.aop.TargetSource#getTargetClass()
	 */
	boolean proxyTargetClass() default false;

}
