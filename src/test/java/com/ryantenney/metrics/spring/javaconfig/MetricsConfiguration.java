/**
 * Copyright (C) 2012 Ryan W Tenney (ryan@10e.us)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ryantenney.metrics.spring.javaconfig;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.codahale.metrics.JmxReporter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.SharedMetricRegistries;
import com.codahale.metrics.health.HealthCheckRegistry;
import com.codahale.metrics.health.SharedHealthCheckRegistries;
import com.ryantenney.metrics.spring.config.annotation.EnableMetrics;
import com.ryantenney.metrics.spring.config.annotation.MetricsConfigurerAdapter;
import org.springframework.jmx.access.MBeanProxyFactoryBean;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

@Configuration
@EnableMetrics
public class MetricsConfiguration extends MetricsConfigurerAdapter {

	@Override
	public void configureReporters(MetricRegistry metricRegistry) {
		registerReporter(JmxReporter.forRegistry(metricRegistry).build()).start();
	}

	@Override
	public MetricRegistry getMetricRegistry() {
		return SharedMetricRegistries.getOrCreate("metricRegistry");
	}

	@Override
	public HealthCheckRegistry getHealthCheckRegistry() {
		return SharedHealthCheckRegistries.getOrCreate("healthCheckRegistry");
	}

	@Bean
	public MetricService getMetricService() {
		return new MetricServiceImpl();
	}

	@Bean(name = "methodGauge")
	public MBeanProxyFactoryBean getMethodGauge() {
		MBeanProxyFactoryBean factoryBean = new MBeanProxyFactoryBean();
		try {
			factoryBean.setObjectName(new ObjectName("metrics", "name", "com.ryantenney.metrics.spring.javaconfig.MetricServiceImpl.gaugedMethod"));
		}
		catch (MalformedObjectNameException e) {
			throw new RuntimeException(e);
		}
		factoryBean.setProxyInterface(JmxReporter.JmxGaugeMBean.class);
		return factoryBean;
	}

	@Bean(name = "fieldGauge")
	public MBeanProxyFactoryBean getFieldGauge() {
		MBeanProxyFactoryBean factoryBean = new MBeanProxyFactoryBean();
		try {
			factoryBean.setObjectName(new ObjectName("metrics", "name", "com.ryantenney.metrics.spring.javaconfig.MetricServiceImpl.gaugeValue"));
		}
		catch (MalformedObjectNameException e) {
			throw new RuntimeException(e);
		}
		factoryBean.setProxyInterface(JmxReporter.JmxGaugeMBean.class);
		return factoryBean;
	}

	@Bean(name = "cachedMethodGauge")
	public MBeanProxyFactoryBean getCachedMethodGauge() {
		MBeanProxyFactoryBean factoryBean = new MBeanProxyFactoryBean();
		try {
			factoryBean.setObjectName(new ObjectName("metrics", "name", "com.ryantenney.metrics.spring.javaconfig.MetricServiceImpl.cachedGaugeMethod"));
		}
		catch (MalformedObjectNameException e) {
			throw new RuntimeException(e);
		}
		factoryBean.setProxyInterface(JmxReporter.JmxGaugeMBean.class);
		return factoryBean;
	}

	@Bean(name = "timer")
	public MBeanProxyFactoryBean getTimer() {
		MBeanProxyFactoryBean factoryBean = new MBeanProxyFactoryBean();
		try {
			factoryBean.setObjectName(new ObjectName("metrics", "name", "com.ryantenney.metrics.spring.javaconfig.MetricServiceImpl.timedMethod"));
		}
		catch (MalformedObjectNameException e) {
			throw new RuntimeException(e);
		}
		factoryBean.setProxyInterface(JmxReporter.JmxTimerMBean.class);
		return factoryBean;
	}

	@Bean
	public MBeanProxyFactoryBean getCounter() {
		MBeanProxyFactoryBean factoryBean = new MBeanProxyFactoryBean();
		try {
			factoryBean.setObjectName(new ObjectName("metrics", "name", "com.ryantenney.metrics.spring.javaconfig.MetricServiceImpl.countedMethod"));
		}
		catch (MalformedObjectNameException e) {
			throw new RuntimeException(e);
		}
		factoryBean.setProxyInterface(JmxReporter.JmxCounterMBean.class);
		return factoryBean;
	}

	@Bean(name = "meter")
	public MBeanProxyFactoryBean getMeter() {
		MBeanProxyFactoryBean factoryBean = new MBeanProxyFactoryBean();
		try {
			factoryBean.setObjectName(new ObjectName("metrics", "name", "com.ryantenney.metrics.spring.javaconfig.MetricServiceImpl.meteredMethod"));
		}
		catch (MalformedObjectNameException e) {
			throw new RuntimeException(e);
		}
		factoryBean.setProxyInterface(JmxReporter.JmxMeterMBean.class);
		return factoryBean;
	}
}
