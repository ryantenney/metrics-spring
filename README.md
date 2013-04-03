#Metrics for Spring [![Build Status](https://secure.travis-ci.org/ryantenney/metrics-spring.png)](http://travis-ci.org/ryantenney/metrics-spring)
=================================

##About

The `metrics-spring` module integrates [Yammer Metrics](http://metrics.codahale.com/) with Spring AOP, and provides XML and Java configuration.

This module does the following things:

* Proxies beans which contain methods annotated with `@Timed`, `@Metered`, and `@ExceptionMetered`
* Registers a `Gauge` for beans which have members annotated with `@Gauge`
* Autowires Timers, Meters, Counters and Histograms into fields annotated with `@InjectMetric`
* Registers with the `HealthCheckRegistry` any beans which extend the class `HealthCheck`

###Maven

Current release version is 2.1.4, which works with Metrics 2.x.

```xml
<dependency>
	<groupId>com.ryantenney.metrics</groupId>
	<artifactId>metrics-spring</artifactId>
	<version>2.1.4</version>
</dependency>
```

Currently working on 3.0.0-SNAPSHOT for Metrics 3 compatibility.

```xml
<repository>
	<id>sonatype-nexus-snapshots</id>
	<name>Sonatype Nexus Snapshots</name>
	<url>http://oss.sonatype.org/content/repositories/snapshots</url>
</repository>

<dependency>
	<groupId>com.ryantenney.metrics</groupId>
	<artifactId>metrics-spring</artifactId>
	<version>3.0.0-SNAPSHOT</version>
</dependency>
```

This module was formerly contained in the [Yammer Metrics repository](https://github.com/codahale/metrics).

###Basic Usage

**Pull requests to improve or expand this documentation would be most welcome.**

Spring Context XML:

```xml
<beans xmlns="http://www.springframework.org/schema/beans"
	   xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	   xmlns:metrics="http://www.ryantenney.com/schema/metrics"
	   xsi:schemaLocation="
			http://www.springframework.org/schema/beans
			http://www.springframework.org/schema/beans/spring-beans-3.2.xsd
			http://www.ryantenney.com/schema/metrics
			http://www.ryantenney.com/schema/metrics/metrics-3.0.xsd">

	<metrics:annotation-driven />

	<!-- beans -->

</beans>
```

Java Config (available in 3.0.0-SNAPSHOT):

```java
import org.springframework.context.annotation.Configuration;
import com.ryantenney.metrics.spring.config.annotation.EnableMetrics;

@Configuration
@EnableMetrics
public class SpringConfiguringClass {
	// ...
}
```

###XML Config

The `<metrics:annotation-driven />` element is required, and has 4 optional arguments:

* `metric-registry` - the id of the `MetricRegsitry` bean with which the generated metrics should be registered. If omitted a new `MetricRegistry` bean is created.
* `health-check-registry` - the id of the `HealthCheckRegsitry` bean with which to register any beans which extend the class `HealthCheck`. If omitted a new `HealthCheckRegistry` bean is created.
* `proxy-target-class` - if set to true, always creates CGLIB proxies instead of defaulting to JDK proxies. This *may* be necessary if you use class-based autowiring.
* `expose-proxy` - if set to true, the target can access the proxy which wraps it by calling `AopContext.currentProxy()`.

The element `<metrics:reporter />` currently doesn't do a damn thing, but it will soon!

###A Note on the Limitations of Spring AOP

Due to limitations of Spring AOP only public methods can be proxied, so `@Timed`, `@Metered`, and `@ExceptionMetered` have no effect on non-public methods. Additionally, calling an annotated method from within the same class will not go through the proxy.

```java
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
```

As `@Gauge` doesn’t involve a proxy, it may be used on non-public fields and methods.
Additionally, `@InjectMetric` may be used on non-public, non-final fields.

---

### License

Copyright (c) 2012 Ryan Tenney, Martello Technologies

Published under Apache Software License 2.0, see LICENSE

[![Rochester Made](http://rochestermade.com/media/images/rochester-made-dark-on-light.png)](http://rochestermade.com)
