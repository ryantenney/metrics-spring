package com.ryantenney.metrics.spring.servlets;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheckRegistry;
import com.codahale.metrics.servlets.MetricsServlet;
import com.codahale.metrics.servlets.HealthCheckServlet;

public class MetricsServletsContextListener implements ServletContextListener {

	@Autowired
	private MetricRegistry metricRegistry;

	@Autowired
	private HealthCheckRegistry healthCheckRegistry;

	private final MetricsServletContextListener metricsServletContextListener = new MetricsServletContextListener();
	private final HealthCheckServletContextListener healthCheckServletContextListener = new HealthCheckServletContextListener();

	@Override
	public void contextInitialized(ServletContextEvent event) {
		WebApplicationContextUtils
			.getRequiredWebApplicationContext(event.getServletContext())
			.getAutowireCapableBeanFactory()
			.autowireBean(this);

		metricsServletContextListener.contextInitialized(event);
		healthCheckServletContextListener.contextInitialized(event);
	}

	@Override
	public void contextDestroyed(ServletContextEvent event) {
	}

	class MetricsServletContextListener extends MetricsServlet.ContextListener {

		@Override
		protected MetricRegistry getMetricRegistry() {
			return metricRegistry;
		}

	}

	class HealthCheckServletContextListener extends HealthCheckServlet.ContextListener {

		@Override
		protected HealthCheckRegistry getHealthCheckRegistry() {
			return healthCheckRegistry;
		}

	}

}
