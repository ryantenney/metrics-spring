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

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.MethodInvokingFactoryBean;

/**
 * This class is needed to allow a AbstractSingleBeanDefinitionParser to invoke methods with variable number of
 * arguments due to limitations with addPropertyReference
 * 
 * @see MetricSetBeanDefinitionParser
 *
 */
public final class SimpleMethodInvokingFactoryBean extends MethodInvokingFactoryBean {
    private final Logger log = LoggerFactory.getLogger(SimpleMethodInvokingFactoryBean.class);
    private Object arg0 = null;
    private Object arg1 = null;
    private Object arg2 = null;
    private Object arg3 = null;
    private Object arg4 = null;
    private boolean failureShouldThrow = true;
    public SimpleMethodInvokingFactoryBean() {
        super();
    }
    public Object getArg0() {
        return arg0;
    }
    public void setArg0(Object arg0) {
        this.arg0 = arg0;
    }
    public Object getArg1() {
        return arg1;
    }
    public void setArg1(Object arg1) {
        this.arg1 = arg1;
    }
    public Object getArg2() {
        return arg2;
    }
    public void setArg2(Object arg2) {
        this.arg2 = arg2;
    }
    public Object getArg3() {
        return arg3;
    }
    public void setArg3(Object arg3) {
        this.arg3 = arg3;
    }
    public Object getArg4() {
        return arg4;
    }
    public void setArg4(Object arg4) {
        this.arg4 = arg4;
    }
    public boolean isFailureShouldThrow() {
        return failureShouldThrow;
    }
    public void setFailureShouldThrow(boolean failureShouldThrow) {
        this.failureShouldThrow = failureShouldThrow;
    }
    
    @Override
    public Object[] getArguments() {
        List<Object> arguments = new ArrayList<Object>();
        if (arg0 != null) arguments.add(arg0);
        if (arg1 != null) arguments.add(arg1);
        if (arg2 != null) arguments.add(arg2);
        if (arg3 != null) arguments.add(arg3);
        if (arg4 != null) arguments.add(arg4);
        return arguments.toArray();
    }
    @Override
    public void afterPropertiesSet() throws Exception {
        try {
            super.afterPropertiesSet();
        } catch (Exception e) {
            log.error("Unexpected error invoking method:{}" + e.getMessage());
            log.debug("Detailed error",e);
            if (failureShouldThrow) {
                throw e;
            }
        }
    }
    
}
