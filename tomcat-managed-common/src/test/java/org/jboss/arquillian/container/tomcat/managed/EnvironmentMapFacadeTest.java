/*
 * JBoss, Home of Professional Open Source
 * Copyright 2014, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.arquillian.container.tomcat.managed;

import static org.assertj.core.api.Assertions.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

public class EnvironmentMapFacadeTest {

    private static final String TEST_KEY = "TEST_KEY";

    private static final String TEST_VALUE = "TEST_VALUE";

    private static final String TEST_APPEND_VALUE = "TEST_APPEND_VALUE";

    private Map<String, String> environmentMap;

    private EnvironmentMapFacade environmentFacade;

    private static final String EXPECTED_APPENDED_VALUE = TEST_VALUE + " " + TEST_APPEND_VALUE;

    @Before
    public void setUp() {

        environmentMap = new HashMap<String, String>();
        environmentFacade = new EnvironmentMapFacade(environmentMap);
    }

    @Test
    public void testPutForValidValue() {

        assertThat(environmentMap).isEmpty();

        environmentFacade.put(TEST_KEY, TEST_VALUE);

        assertThat(environmentMap).containsOnly(entry(TEST_KEY, TEST_VALUE));
    }

    @Test
    public void testPutForBlankValue() {

        assertThat(environmentMap).isEmpty();

        environmentFacade.put(TEST_KEY, "");

        assertThat(environmentMap).isEmpty();
    }

    @Test
    public void testPutForNullValue() {

        assertThat(environmentMap).isEmpty();

        environmentFacade.put(TEST_KEY, null);

        assertThat(environmentMap).isEmpty();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPutForNullKey() {

        environmentFacade.put(null, TEST_VALUE);
    }

    @Test
    public void testAppendForExistingKeyAndValidValue() {

        assertThat(environmentMap).isEmpty();

        environmentMap.put(TEST_KEY, TEST_VALUE);

        environmentFacade.append(TEST_KEY, TEST_APPEND_VALUE);

        assertThat(environmentMap).containsOnly(entry(TEST_KEY, EXPECTED_APPENDED_VALUE));
    }

    @Test
    public void testAppendForMissingKeyAndValidValue() {

        assertThat(environmentMap).isEmpty();

        environmentFacade.append(TEST_KEY, TEST_APPEND_VALUE);

        assertThat(environmentMap).containsOnly(entry(TEST_KEY, TEST_APPEND_VALUE));
    }

    @Test
    public void testAppendForExistingKeyAndBlankValue() {

        assertThat(environmentMap).isEmpty();

        environmentMap.put(TEST_KEY, TEST_VALUE);

        environmentFacade.append(TEST_KEY, "");

        assertThat(environmentMap).containsOnly(entry(TEST_KEY, TEST_VALUE));
    }

    @Test
    public void testAppendForMissingKeyAndBlankValue() {

        assertThat(environmentMap).isEmpty();

        environmentFacade.append(TEST_KEY, "");

        assertThat(environmentMap).isEmpty();
    }

    @Test
    public void testAppendForExistingKeyAndNullValue() {

        assertThat(environmentMap).isEmpty();

        environmentMap.put(TEST_KEY, TEST_VALUE);

        environmentFacade.append(TEST_KEY, null);

        assertThat(environmentMap).containsOnly(entry(TEST_KEY, TEST_VALUE));
    }

    @Test
    public void testAppendForMissingKeyAndNullValue() {

        assertThat(environmentMap).isEmpty();

        environmentFacade.append(TEST_KEY, null);

        assertThat(environmentMap).isEmpty();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAppendForNullKey() {

        environmentFacade.append(null, TEST_VALUE);
    }
}
