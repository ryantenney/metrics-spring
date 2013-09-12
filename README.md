#Metrics for Spring [![Build Status](https://secure.travis-ci.org/ryantenney/metrics-spring.png)](http://travis-ci.org/ryantenney/metrics-spring)

##About

The `metrics-spring` module integrates [Yammer Metrics](http://metrics.codahale.com/) with Spring AOP, and provides XML and Java configuration.

This module does the following things:

* Proxies beans which contain methods annotated with `@Timed`, `@Metered`, `@ExceptionMetered`, and `@Counted`
* Registers a `Gauge` for beans which have members annotated with `@Gauge`
* Autowires Timers, Meters, Counters and Histograms into fields annotated with `@InjectMetric`
* Registers with the `HealthCheckRegistry` any beans which extend the class `HealthCheck`
* Creates reporters and binds them to the Spring lifecycle

###Maven

Current version is 3.0.0-RC2, which is compatible with Metrics 3.0

```xml
<dependency>
	<groupId>com.ryantenney.metrics</groupId>
	<artifactId>metrics-spring</artifactId>
	<version>3.0.0-RC2</version>
</dependency>
```

This module was formerly contained in the [Yammer Metrics repository](https://github.com/codahale/metrics).

###Basic Usage

As of version 3, `metrics-spring` may be configured using XML or Java, depending on your personal preference.

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

	<!-- registry and reporters should be defined in only one context xml file -->
	<metrics:metric-registry id="registry" name="springMetrics" />
	<metrics:reporter type="console" metric-registry="registry" period="1m" />
	
	<!-- annotation-driven must be included in all context files -->
	<metrics:annotation-driven metric-registry="registry" />

	<!-- beans -->

</beans>
```

Java Annotation Config:

```java
import java.util.concurrent.TimeUnit;
import org.springframework.context.annotation.Configuration;
import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.SharedMetricRegistries;
import com.ryantenney.metrics.spring.config.annotation.EnableMetrics;
import com.ryantenney.metrics.spring.config.annotation.MetricsConfigurerAdapter;

@Configuration
@EnableMetrics
public class SpringConfiguringClass extends MetricsConfigurerAdapter {

	@Override
	public MetricRegistry getMetricRegistry() {
		return SharedMetricRegistries.getOrCreate("springMetrics");
	}

	@Override
	public void configureReporters(MetricRegistry metricRegistry) {
		ConsoleReporter.forRegistry(registry)
			.outputTo(output)
			.build()
			.start(1, TimeUnit.MINUTES);
	}

}
```

###XML Config Documentation

The `<metrics:annotation-driven />` element is required, and has 4 optional arguments:

* `metric-registry` - the id of the `MetricRegsitry` bean with which the generated metrics should be registered. If omitted a new `MetricRegistry` bean is created.
* `health-check-registry` - the id of the `HealthCheckRegsitry` bean with which to register any beans which extend the class `HealthCheck`. If omitted a new `HealthCheckRegistry` bean is created.
* `proxy-target-class` - if set to true, always creates CGLIB proxies instead of defaulting to JDK proxies. This *may* be necessary if you use class-based autowiring.
* `expose-proxy` - if set to true, the target can access the proxy which wraps it by calling `AopContext.currentProxy()`.

The `<metrics:metric-registry />` element constructs a new MetricRegistry or retrieves a shared registry:

* `id` - the bean name with which to register the MetricRegistry bean
* `name` - the name of the MetricRegistry, if present, this calls SharedMetricRegistries.getOrCreate(name)

The `<metrics:health-check-registry />` element constructs a new HealthCheckRegistry:

* `id` - the bean name with which to register the HealthCheckRegistry bean

The `<metrics:reporter />` element creates and starts a reporter.

* `id` - the bean name
* `metric-registry` - the id of the `MetricRegsitry` bean for which the reporter should retrieve metrics
* `type` - the type of the reporter. Additional types may be registered through SPI (more on this later).
 * `console`: ConsoleReporter
 * `jmx`: JmxReporter
 * `slf4j`: Slf4jReporter
 * `ganglia`: GangliaReporter (requires `metrics-ganglia`)
 * `graphite`: GraphiteReporter (requires `metrics-graphite`)

###Java Config Documentation

A `@Configuration` class annotated with `@EnableMetrics` is functionally equivalent to using the `<metrics:annotation-driven />` element.

* `proxyTargetClass` - if set to true, always creates CGLIB proxies instead of defaulting to JDK proxies. This *may* be necessary if you use class-based autowiring.
* `exposeProxy` - if set to true, the target can access the proxy which wraps it by calling `AopContext.currentProxy()`.

The class may also implement the interface `MetricsConfigurer`, or extend the abstract class `MetricsConfigurerAdapter`

* `getMetricRegistry()` - return the `MetricRegsitry` instance with which metrics should be registered. If omitted a new `MetricRegistry` instance is created.
* `getHealthCheckRegistry()` - return the `HealthCheckRegsitry` instance with which to register any beans which extend the class `HealthCheck`. If omitted a new `HealthCheckRegistry` instance is created.
* `configureReporters(MetricRegistry)` - configure reporters

###A Note on the Limitations of Spring AOP

Due to limitations of Spring AOP only public methods can be proxied, so `@Timed`, `@Metered`, `@ExceptionMetered`, and `@Counted` have no effect on non-public methods. Additionally, calling an annotated method from within the same class will not go through the proxy.

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

###Users of the Maven Shade plugin

Please see the [Shade Readme](SHADE-README.md)

###Documentation

Javadocs are hosted at http://ryantenney.github.io/metrics-spring/docs/

### License

Copyright (c) 2012-2013 Ryan Tenney, Martello Technologies

Published under Apache Software License 2.0, see LICENSE

[![Rochester Made](http://rochestermade.com/media/images/rochester-made-dark-on-light.png)](http://rochestermade.com)
