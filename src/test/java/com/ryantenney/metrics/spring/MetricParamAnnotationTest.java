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
package com.ryantenney.metrics.spring;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.codahale.metrics.annotation.ExceptionMetered;
import com.codahale.metrics.annotation.Metered;
import com.codahale.metrics.annotation.Timed;
import com.ryantenney.metrics.annotation.Counted;
import com.ryantenney.metrics.annotation.MetricParam;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static com.ryantenney.metrics.spring.TestUtil.*;
import static org.junit.Assert.*;

/**
 * Test the MetricParam annotation. 
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:metric-param-class.xml")
public class MetricParamAnnotationTest {

    @Autowired
    MetricParamAnnotationTest.MetricParamClass metricParamClass;

    MetricRegistry metricRegistry;

    @Autowired
    public void setMetricRegistry(MetricRegistry metricRegistry) {
        this.metricRegistry = metricRegistry;
        this.metricRegistry.addListener(new LoggingMetricRegistryListener());
    }

    @Test
    public void timedParameterMethod() {
        Timer timedMethod = forTimedMethod(metricRegistry, MetricParamClass.class, "timedParameterMethod", 1);
        assertNull(timedMethod);

        metricParamClass.timedParameterMethod(1);
        timedMethod = forTimedMethod(metricRegistry, MetricParamClass.class, "timedParameterMethod", 1);
        assertNotNull(timedMethod);
        assertEquals(1, timedMethod.getCount());

        Method method = findMethod(MetricParamClass.class, "timedParameterMethod");
        String metricName = forTimedMethod(MetricParamClass.class, method, method.getAnnotation(Timed.class), 1);
        assertEquals("timed-param.1", metricName);
    }

    @Test
    public void countedParameterMethod() {
        Counter countedMethod = forCountedMethod(metricRegistry, MetricParamClass.class, "countedParameterMethod", "foo");
        assertNull(countedMethod);

        metricParamClass.countedParameterMethod("foo");
        countedMethod = forCountedMethod(metricRegistry, MetricParamClass.class, "countedParameterMethod", "foo");
        assertNotNull(countedMethod);
        assertEquals(1, countedMethod.getCount());

        Method method = findMethod(MetricParamClass.class, "countedParameterMethod");
        String metricName = forCountedMethod(MetricParamClass.class, method, method.getAnnotation(Counted.class), "foo");
        assertEquals("counted-param.foo", metricName);
    }

    @Test
    public void meteredParameterMethod() {
        Meter meteredMethod = forMeteredMethod(metricRegistry, MetricParamClass.class, "meteredParameterMethod", new BigDecimal(50));
        assertNull(meteredMethod);

        metricParamClass.meteredParameterMethod(new BigDecimal(50));
        meteredMethod = forMeteredMethod(metricRegistry, MetricParamClass.class, "meteredParameterMethod", new BigDecimal(50));
        assertNotNull(meteredMethod);
        assertEquals(1, meteredMethod.getCount());

        Method method = findMethod(MetricParamClass.class, "meteredParameterMethod");
        String metricName = forMeteredMethod(MetricParamClass.class, method, method.getAnnotation(Metered.class), new BigDecimal(50));
        assertEquals("metered-param.50", metricName);
    }

    @Test
    public void exceptionMeteredParameterMethod() {
        Meter meteredMethod = forExceptionMeteredMethod(metricRegistry, MetricParamClass.class, "exceptionMeteredParameterMethod", 5);
        assertNull(meteredMethod);

        //throw wrong exception
        try {
            metricParamClass.exceptionMeteredParameterMethod(RuntimeException.class, 5);
            fail();
        }
        catch (Throwable t) {
            assert t instanceof RuntimeException;
        }
        meteredMethod = forExceptionMeteredMethod(metricRegistry, MetricParamClass.class, "exceptionMeteredParameterMethod", RuntimeException.class, 5);
        assertNotNull(meteredMethod);
        assertEquals(0, meteredMethod.getCount());

        //throw correct exception
        try {
            metricParamClass.exceptionMeteredParameterMethod(BogusException.class, 5);
            fail();
        }
        catch (Throwable t) {
            assert t instanceof BogusException;
        }
        meteredMethod = forExceptionMeteredMethod(metricRegistry, MetricParamClass.class, "exceptionMeteredParameterMethod", BogusException.class, 5);
        assertNotNull(meteredMethod);
        assertEquals(1, meteredMethod.getCount());

        Method method = findMethod(MetricParamClass.class, "exceptionMeteredParameterMethod");
        String metricName = forExceptionMeteredMethod(MetricParamClass.class, method, method.getAnnotation(ExceptionMetered.class), BogusException.class, 5);
        assertEquals("exception-metered-param.5.exceptions", metricName);
    }

    @Test
    public void timedCollectionParameterMethod() {
        List<String> list = Arrays.asList("1", "2", "3");
        Timer timedMethod = forTimedMethod(metricRegistry, MetricParamClass.class, "timedCollectionParameterMethod", list);
        assertNull(timedMethod);

        metricParamClass.timedCollectionParameterMethod(list);
        timedMethod = forTimedMethod(metricRegistry, MetricParamClass.class, "timedCollectionParameterMethod", list);
        assertNotNull(timedMethod);
        assertEquals(1, timedMethod.getCount());

        Method method = findMethod(MetricParamClass.class, "timedCollectionParameterMethod");
        String metricName = forTimedMethod(MetricParamClass.class, method, method.getAnnotation(Timed.class), list);
        assertEquals("timed-collection-param.(3)", metricName);
    }


    public static class MetricParamClass {

        @Timed(absolute = true, name = "timed-param.{param}")
        public void timedParameterMethod(@MetricParam(value = "param") int param) {}

        @Counted(absolute = true, name = "counted-param.{param}", monotonic = true)
        public void countedParameterMethod(@MetricParam(value = "param") String param) {}

        @Metered(absolute = true, name = "metered-param.{param}")
        public void meteredParameterMethod(@MetricParam(value = "param") BigDecimal param) {}

        @ExceptionMetered(cause = BogusException.class, absolute = true, name = "exception-metered-param.{param}")
        public <T extends Throwable> void exceptionMeteredParameterMethod(Class<T> clazz, @MetricParam(value = "param") int param) throws Throwable {
            if (clazz != null) {
                throw clazz.newInstance();
            }
        }

        @Timed(absolute = true, name = "timed-collection-param.{param}")
        public void timedCollectionParameterMethod(@MetricParam(value = "param", collection = true) List<String> params) {}
        
    }

    @SuppressWarnings("serial")
    public static class BogusException extends RuntimeException {}
}
