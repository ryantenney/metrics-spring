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
package com.ryantenney.metrics.spring.reporter;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import static org.junit.Assert.fail;

/**
 * Test for {@link AbstractReporterElementParser#validate(java.util.Map)}
 *
 * @author Dana P'Simer&lt;danap@bluesoftdev.com&gt;
 */
public class AbstractReporterElementParserTest {

    AbstractReporterElementParser target = new AbstractReporterElementParser() {

        @Override
        public String getType() {
            return "test";
        }

        @Override
        protected void validate(AbstractReporterElementParser.ValidationContext c) {
            c.require("required");
            c.require("requiredRegex", "[0-9]*");
            c.optional("optional");
            c.optional("optionalRegex", "[a-zA-z]*", "Must be only alphabetic characters");
            c.rejectUnmatchedProperties();
        }
    };

    @Test
    public void testValidate() {
        Map<String, String> properties = new HashMap<String, String>();
        properties.put("required", "requiredValue");
        properties.put("requiredRegex", "42");
        properties.put("optionalRegex", "abcd");
        target.validate(properties);
    }

    @Test
    public void testValidateNotRequired() {
        Map<String, String> properties = new HashMap<String, String>();
        properties.put("optionalRegex", "abcd");
        try {
            target.validate(properties);
            fail();
        } catch (AbstractReporterElementParser.ValidationException ex) {
        }
    }

    @Test
    public void testValidateBadRequired() {
        Map<String, String> properties = new HashMap<String, String>();
        properties.put("required", "requiredValue");
        properties.put("requriedRegex", "42");
        properties.put("optionalRegex", "abcd");
        try {
            target.validate(properties);
            fail();
        } catch (AbstractReporterElementParser.ValidationException ex) {
        }
    }

    @Test
    public void testValidateBadOptional() {
        Map<String, String> properties = new HashMap<String, String>();
        properties.put("required", "requiredValue");
        properties.put("optionalRegex", "a5bcd");
        try {
            target.validate(properties);
            fail();
        } catch (AbstractReporterElementParser.ValidationException ex) {
        }
    }

    @Test
    public void testValidateWithNamespaceDefinitions() {
        Map<String, String> properties = new HashMap<String, String>();
        properties.put("xmlns:foo", "http://foo.com/foo http://foo.com/foo/v1/foo.xsd");
        properties.put("required", "requiredValue");
        properties.put("requiredRegex", "42");
        properties.put("optionalRegex", "abcd");
        target.validate(properties);
    }
}
