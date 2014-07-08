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
package com.ryantenney.metrics.spring.servlets;

import javax.servlet.ServletContextEvent;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.servlet.InstrumentedFilterContextListener;

/**
 * Spring-friendly subclass of {@link InstrumentedFilterContextListener} to instrument 
 * web applications (to be used with com.codahale.metrics.servlet.InstrumentedFilter).
 * 
 * @author sharath
 */
public class WiredInstrumentedFilterContextListener extends InstrumentedFilterContextListener {

	@Autowired
	private MetricRegistry metricRegistry;

	@Override
	public void contextInitialized(final ServletContextEvent sce) {
		// let spring wire metric-registry
		WebApplicationContextUtils.getRequiredWebApplicationContext(sce.getServletContext()).getAutowireCapableBeanFactory().autowireBean(this);

		// trigger the wired metric-registry to be set in servlet-context.
		super.contextInitialized(sce);
	}

	@Override
	protected MetricRegistry getMetricRegistry() {
		return metricRegistry;
	}
}