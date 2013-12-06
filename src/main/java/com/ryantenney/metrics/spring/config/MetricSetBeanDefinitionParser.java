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
package com.ryantenney.metrics.spring.config;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.w3c.dom.Element;

/**
 * Registers a metric set or a single metric
 * 
 * Note: name is required if adding a single metric
 */
class MetricSetBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {

    protected void doParse(Element element, BeanDefinitionBuilder bean) {
        // this will never be null since the schema explicitly requires that a value be supplied
        String name = element.getAttribute("name");
        String ref = element.getAttribute("ref");
        String metricRegistryName = element.getAttribute("metric-registry");

        bean.addDependsOn(metricRegistryName);
        bean.addDependsOn(ref);
        
        bean.addPropertyReference("targetObject", metricRegistryName);
        if (name == null) {
            bean.addPropertyValue("targetMethod", "registerAll");
            bean.addPropertyReference("arg0",ref);
        } else {
            bean.addPropertyValue("targetMethod", "register");
            bean.addPropertyValue("arg0",name);
            bean.addPropertyReference("arg1",ref);
        }
        // Unfortunately spring invokes this method more than once
        // TODO: check if main branch implementation fixes this or not
        bean.addPropertyValue("failureShouldThrow", false);
     }

	@Override
	protected boolean shouldGenerateIdAsFallback() {
		return true;
	}
	protected Class getBeanClass(Element element) {
	    return SimpleMethodInvokingFactoryBean.class;
	}
	


}
