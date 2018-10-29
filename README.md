# Metrics for Spring

[![Build Status](https://img.shields.io/travis/ryantenney/metrics-spring/master.svg?style=flat-square)](https://travis-ci.org/ryantenney/metrics-spring)
![Maven Central](https://img.shields.io/maven-central/v/com.ryantenney.metrics/metrics-spring.svg?style=flat-square)
![GitHub license](https://img.shields.io/github/license/ryantenney/metrics-spring.svg?style=flat-square)

## About

The `metrics-spring` module integrates [Dropwizard Metrics library](http://metrics.dropwizard.io/) with Spring, and provides XML and Java configuration.

This module does the following:

* Creates metrics and proxies beans which contain methods annotated with `@Timed`, `@Metered`, `@ExceptionMetered`, and `@Counted`
* Registers a `Gauge` for beans which have members annotated with `@Gauge` and `@CachedGauge`
* Autowires Timers, Meters, Counters and Histograms into fields annotated with `@Metric`
* Registers with the `HealthCheckRegistry` any beans which extend the class `HealthCheck`
* Creates reporters from XML config and binds them to the Spring lifecycle
* Registers metrics and metric sets in XML

### Maven

Current version is 3.9.9, which is compatible with Metrics 4.0.2

```xml
<dependency>
    <groupId>com.ryantenney.metrics</groupId>
    <artifactId>metrics-spring</artifactId>
    <version>3.9.9</version>
</dependency>
```

### Basic Usage

As of version 3, `metrics-spring` may be configured using XML or Java, depending on your personal preference.

Spring Context XML:

```xml
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:metrics="http://www.ryantenney.com/schema/metrics"
       xsi:schemaLocation="
           http://www.springframework.org/schema/beans
           http://www.springframework.org/schema/beans/spring-beans.xsd
           http://www.ryantenney.com/schema/metrics
           http://www.ryantenney.com/schema/metrics/metrics.xsd">

    <!-- Creates a MetricRegistry bean -->
    <metrics:metric-registry id="metricRegistry" />

    <!-- Creates a HealthCheckRegistry bean (Optional) -->
    <metrics:health-check-registry id="health" />

    <!-- Registers BeanPostProcessors with Spring which proxy beans and capture metrics -->
    <!-- Include this once per context (once in the parent context and in any subcontexts) -->
    <metrics:annotation-driven metric-registry="metricRegistry" />

    <!-- Example reporter definiton. Supported reporters include jmx, slf4j, graphite, and others. -->
    <!-- Reporters should be defined only once, preferably in the parent context -->
    <metrics:reporter type="console" metric-registry="metricRegistry" period="1m" />

    <!-- Register metric beans (Optional) -->
    <!-- The metrics in this example require metrics-jvm -->
    <metrics:register metric-registry="metricRegistry">
        <bean metrics:name="jvm.gc" class="com.codahale.metrics.jvm.GarbageCollectorMetricSet" />
        <bean metrics:name="jvm.memory" class="com.codahale.metrics.jvm.MemoryUsageGaugeSet" />
        <bean metrics:name="jvm.thread-states" class="com.codahale.metrics.jvm.ThreadStatesGaugeSet" />
        <bean metrics:name="jvm.fd.usage" class="com.codahale.metrics.jvm.FileDescriptorRatioGauge" />
    </metrics:register>

    <!-- Beans and other Spring config -->

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
    public void configureReporters(MetricRegistry metricRegistry) {
        // registerReporter allows the MetricsConfigurerAdapter to
        // shut down the reporter when the Spring context is closed
        registerReporter(ConsoleReporter
            .forRegistry(metricRegistry)
            .build())
            .start(1, TimeUnit.MINUTES);
    }

}
```

### XML Config Documentation

The `<metrics:annotation-driven />` element is required, and has 4 optional arguments:
* Attributes
 * `metric-registry` - the id of the `MetricRegistry` bean with which the generated metrics should be registered. If omitted a new `MetricRegistry` bean is created.
 * `health-check-registry` - the id of the `HealthCheckRegistry` bean with which to register any beans which extend the class `HealthCheck`. If omitted a new `HealthCheckRegistry` bean is created.
 * `proxy-target-class` - if set to true, always creates CGLIB proxies instead of defaulting to JDK proxies. This *may* be necessary if you use class-based autowiring.
 * `expose-proxy` - if set to true, the target can access the proxy which wraps it by calling `AopContext.currentProxy()`.

The `<metrics:metric-registry />` element constructs a new MetricRegistry or retrieves a shared registry:
* Attributes
 * `id` - the bean name with which to register the MetricRegistry bean
 * `name` - the name of the MetricRegistry, if present, this calls SharedMetricRegistries.getOrCreate(name)

The `<metrics:health-check-registry />` element constructs a new HealthCheckRegistry:
* Attributes
 * `id` - the bean name with which to register the HealthCheckRegistry bean

The `<metrics:reporter />` element creates and starts a reporter:
* Attributes
 * `id` - the bean name
 * `metric-registry` - the id of the `MetricRegistry` bean for which the reporter should retrieve metrics
 * `type` - the type of the reporter. Additional types may be registered through SPI (more on this later).
  * `console`: ConsoleReporter
  * `jmx`: JmxReporter
  * `slf4j`: Slf4jReporter
  * `graphite`: GraphiteReporter (requires `metrics-graphite`)

The `<metrics:register />` element registers with the MetricRegistry a bean which extends implements Metric or MetricSet
* Attributes
 * `metric-registry` - the id of the `MetricRegistry` bean with which the metrics are to be registered
* Child elements
 * `<bean />` - The beans to register with the specified registry.
  * `metrics:name` attribute on the bean element - specifies the name with which the metric will be registered. Optional if the bean is a MetricSet.

### Java Config Documentation

A `@Configuration` class annotated with `@EnableMetrics` is functionally equivalent to using the `<metrics:annotation-driven />` element.

* `proxyTargetClass` - if set to true, always creates CGLIB proxies instead of defaulting to JDK proxies. This *may* be necessary if you use class-based autowiring.
* `exposeProxy` - if set to true, the target can access the proxy which wraps it by calling `AopContext.currentProxy()`.

The class may also implement the interface `MetricsConfigurer`, or extend the abstract class `MetricsConfigurerAdapter`

* `getMetricRegistry()` - return the `MetricRegistry` instance with which metrics should be registered. If omitted a new `MetricRegistry` instance is created.
* `getHealthCheckRegistry()` - return the `HealthCheckRegistry` instance with which to register any beans which extend the class `HealthCheck`. If omitted a new `HealthCheckRegistry` instance is created.
* `configureReporters(MetricRegistry)` - configure reporters

### A Note on the Limitations of Spring AOP

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

### Users of the Maven Shade plugin

Please see the [Shade Readme](SHADE-README.md)

### Documentation

Javadocs are hosted at http://ryantenney.github.io/metrics-spring/docs/


### Acknowledgements

[![YourKit](https://www.yourkit.com/images/yklogo.png)](https://www.yourkit.com/)

YourKit is kindly supporting this open source project with its full-featured Java Profiler.
YourKit, LLC is the creator of innovative and intelligent tools for profiling
Java and .NET applications. Take a look at YourKit's leading software products:
[YourKit Java Profiler](http://www.yourkit.com/java/profiler/index.jsp) and
[YourKit .NET Profiler](http://www.yourkit.com/.net/profiler/index.jsp).


### License

Copyright (c) 2012-2017 Ryan Tenney

Portions Copyright (c) 2012-2013 Martello Technologies

Published under Apache Software License 2.0, see LICENSE

[![Rochester Made](http://rochestermade.com/media/images/rochester-made-dark-on-light.png)](http://rochestermade.com)
