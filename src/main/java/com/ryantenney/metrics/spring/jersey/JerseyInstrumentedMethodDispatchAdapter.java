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
package com.ryantenney.metrics.spring.jersey;

import java.util.Set;

import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.SharedMetricRegistries;
import com.codahale.metrics.jersey.InstrumentedResourceMethodDispatchAdapter;
import com.ryantenney.metrics.spring.config.MetricRegistryBeanDefinitionParser;

/**
 * Autowire in the jaxrs provider to support @Timed within jersey
 * Need to include the following in your web.xml:
 *  <servlet>
 *       <servlet-name>jersey</servlet-name>
 *       <servlet-class>
 *           com.sun.jersey.spi.spring.container.servlet.SpringServlet</servlet-class>
 *       <init-param>
 *           <param-name>com.sun.jersey.config.property.resourceConfigClass</param-name>
 *           <param-value>com.sun.jersey.api.core.PackagesResourceConfig</param-value>
 *       </init-param>
 *       <init-param>
 *           <param-name>com.sun.jersey.config.property.packages</param-name>
 *           <param-value>com.ryantenney</param-value>
 *       </init-param>
 *   </servlet>
 *
 * NOTE: Do not scan the metrics-jersey packages or you will get a NPE
 * 
 */
@Provider
public class JerseyInstrumentedMethodDispatchAdapter extends InstrumentedResourceMethodDispatchAdapter {
    private static Logger logger = LoggerFactory.getLogger(JerseyInstrumentedMethodDispatchAdapter.class);
     
    
    public JerseyInstrumentedMethodDispatchAdapter() {
        //todo: find a way to wire in a differently named metric registry
        this(getMetricsRegristry());
    }
    public JerseyInstrumentedMethodDispatchAdapter(String name) {
        this(SharedMetricRegistries.getOrCreate(name));
    }

    public JerseyInstrumentedMethodDispatchAdapter(MetricRegistry registry) {
        super(registry);
        logger.info("Instrumenting jersey classes");
    }

    private  static MetricRegistry getMetricsRegristry() {
        Set<String> names = SharedMetricRegistries.names();
        final String name;
        if (names.size() == 0) {
            name = MetricRegistryBeanDefinitionParser.DEFAULT_NAME;
            logger.warn("Could not find a shared metrics registry, using default name {}", name);
        } else if (names.size() == 1) {
            name = names.iterator().next();
            logger.info("Using metric registry {}", name);
        } else {
            name = names.iterator().next();
            logger.warn("Multiple metrics registries have beenCould not find a shared metrics registry, taking first name {}", name);
        }
        return SharedMetricRegistries.getOrCreate(name);
    }

}
