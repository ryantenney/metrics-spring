#Metrics for Spring [![Build Status](https://secure.travis-ci.org/ryantenney/metrics-spring.png)](http://travis-ci.org/ryantenney/metrics-spring)
=================================

##About

The `metrics-spring` module integrates [Yammer Metrics](http://metrics.codahale.com/) with Spring AOP, complete with simple XML configuration.

This module does the following things:

* Proxies beans which contain methods annotated with `@Timed`, `@Metered`, and `@ExceptionMetered`
* Registers a `Gauge` for beans which have members annotated with `@Gauge`
* Registers with the `HealthCheckRegistry` any beans which extend the class `HealthCheck`

###Maven

	<dependency>
		<groupId>com.yammer.metrics</groupId>
		<artifactId>metrics-spring</artifactId>
		<version>2.1.2</version>
	</dependency>

This module was formerly contained in the [Yammer Metrics repository](https://github.com/codahale/metrics). Version 2.1.x will be the last version of this module available at these coordinates. This is the new official home of this project, and new coordinates will be available soon.

###Basic Usage

Spring Context XML:

	<beans xmlns="http://www.springframework.org/schema/beans"
		   xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
		   xmlns:metrics="http://www.yammer.com/schema/metrics"
		   xsi:schemaLocation="
				http://www.springframework.org/schema/beans
				http://www.springframework.org/schema/beans/spring-beans-3.1.xsd
				http://www.yammer.com/schema/metrics
				http://www.yammer.com/schema/metrics/metrics.xsd">
	
		<metrics:annotation-driven />
	
		<!-- beans -->
	
	</beans>

###XML Config

The `<metrics:annotation-driven />` element is required, and has 5 optional arguments:

* `metrics-registry` - the id of the `MetricsRegsitry` bean with which the generated metrics should be registered. If omitted, this defaults to registry provided by `Metrics.defaultRegistry()`.
* `health-check-registry` - the id of the `HealthCheckRegsitry` bean with which to register any beans which extend the class `HealthCheck`. If omitted, this defaults to the registry provided by `HealthChecks.defaultRegistry()`.
* `scope` - sets the scope for each of the metrics.
* `proxy-target-class` - if set to true, always creates CGLIB proxies instead of defaulting to JDK proxies. This *may* be necessary if you use class-based autowiring.
* `expose-proxy` - if set to true, the target can access the proxy which wraps it by calling `AopContext.currentProxy()`.

The elements `<metrics:metrics-registry />` and `<metrics:health-check-registry />` are present as a convenience for creating new registry beans.

The element `<metrics:jmx-reporter />` creates a JMX Reporter for the specified metrics registry. A JMX Reporter is automatically created for the default metrics registry.

###A Note on the Limitations of Spring AOP

Due to limitations of Spring AOP only public methods can be proxied, so `@Timed`, `@Metered`, and `@ExceptionMetered` have no effect on non-public methods. Additionally, calling an annotated method from within the same class will not go through the proxy.

	public class Foo {
		
		@Timed
		public void bar() { /* … */ }
		
		public void baz() {
			this.bar(); // doesn't pass through the proxy
			
			// fix: reengineer
			// workaround: enable `expose-proxy` and change to:
			((Foo) AopContext.currentProxy()).bar(); // hideous, but it works
		}
	}

As `@Gauge` doesn’t involve a proxy, it is possible to use this annotation on private fields and methods.

##License
=========

Copyright (c) 2012 Ryan Tenney, Martello Technologies

Published under Apache Software License 2.0, see LICENSE
