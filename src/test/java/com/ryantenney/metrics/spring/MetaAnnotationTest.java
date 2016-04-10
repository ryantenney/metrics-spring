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

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.codahale.metrics.annotation.ExceptionMetered;
import com.codahale.metrics.annotation.Metered;
import com.codahale.metrics.annotation.Timed;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.SortedSet;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:meta-annotation.xml")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class MetaAnnotationTest {

    private static final String ERROR_MESSAGE = "error message";

    private static final int EXPECTED_NUMBERS_OF_KEYS = 3;

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @Metered
    @ExceptionMetered(cause = RuntimeException.class)
    public @interface MetaAnnotationMeteredAndExceptionMetered {

    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @Timed
    public @interface MetaAnnotationTimed {

    }

    public static class MetaAnnotatedClass {

        public MetaAnnotatedClass() {
        }

        @MetaAnnotationMeteredAndExceptionMetered
        public void doItMetered() {
            throw new RuntimeException(ERROR_MESSAGE);
        }

        @MetaAnnotationTimed
        public void doItTimed() {

        }

    }

    @Autowired
    MetricRegistry metricRegistry;

    @Autowired
    MetaAnnotatedClass metaAnnotatedClass;

    @Test
    public void assureThatMetaAnnotationsWorkForMeteredAndExceptionMetered() {

        try {
            metaAnnotatedClass.doItMetered();
            Assert.fail("exception expected");
        } catch (RuntimeException e) {
            Assert.assertEquals(ERROR_MESSAGE, e.getMessage());
            Assert.assertEquals(EXPECTED_NUMBERS_OF_KEYS, metricRegistry.getNames().size());
            assertThatDoItMeteredIsCalled(1);
            assertThatDoItMeteredExceptionIsCalled(1);
            assertThatDoItTimedIsCalled(0);
        }
    }

    @Test
    public void assureThatMetaAnnotationsWorkForTimed() {

        metaAnnotatedClass.doItTimed();
        Assert.assertEquals(EXPECTED_NUMBERS_OF_KEYS, metricRegistry.getNames().size());
        assertThatDoItMeteredIsCalled(0);
        assertThatDoItMeteredExceptionIsCalled(0);
        assertThatDoItTimedIsCalled(1);
    }

    private void assertThatDoItTimedIsCalled(int times) {

        String key = assertThatMetricRegistryContains("doItTimed");
        assertThatTimerIsCalled(key, times);
    }

    private void assertThatDoItMeteredExceptionIsCalled(int times) {

        String key = assertThatMetricRegistryContains("doItMetered.exceptions");
        assertThatMeterIsCalled(key, times);
    }

    private void assertThatDoItMeteredIsCalled(int times) {

        String key = assertThatMetricRegistryContains("doItMetered");
        assertThatMeterIsCalled(key, times);
    }

    private void assertThatTimerIsCalled(String key, int times) {

        Timer meter = metricRegistry.getTimers().get(key);
        Assert.assertEquals(times, meter.getCount());
    }

    private void assertThatMeterIsCalled(String key, int times) {

        Meter meter = metricRegistry.getMeters().get(key);
        Assert.assertEquals(times, meter.getCount());
    }

    private String assertThatMetricRegistryContains(String suffix) {

        String prefix = MetaAnnotatedClass.class.getCanonicalName();
        SortedSet<String> names = metricRegistry.getNames();
        String key = prefix + "." + suffix;
        Assert.assertTrue(names.contains(key));
        return key;
    }

}
