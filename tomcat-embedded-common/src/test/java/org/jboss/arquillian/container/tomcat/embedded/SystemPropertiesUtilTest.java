package org.jboss.arquillian.container.tomcat.embedded;

import static org.junit.Assert.*;

import org.junit.BeforeClass;
import org.junit.Test;

public class SystemPropertiesUtilTest {

    private static final String TEST_KEY = SystemPropertiesUtilTest.class.getName() + ".TEST_KEY";

    private static final String TEST_VALUE = SystemPropertiesUtilTest.class.getName() + ".TEST_VALUE";

    private static final String TEST_NO_ENV_ORIGINAL = TEST_KEY;

    private static final String TEST_NO_ENV_EXPECTED = TEST_NO_ENV_ORIGINAL;

    private static final String TEST_ENV_ORIGINAL = "${env." + TEST_KEY + "}";

    private static final String TEST_ENV_EXPECTED = TEST_VALUE;

    private static final SystemPropertiesUtil systemPropertiesUtil = new SystemPropertiesUtil();

    @BeforeClass
    public static void beforeClass() {

        System.setProperty(TEST_KEY, TEST_VALUE);
    }

    @Test
    public void testSubstituteEnvironmentVariableForNoVariable() {

        testSubstituteEnvironmentVariable(TEST_NO_ENV_ORIGINAL, TEST_NO_ENV_EXPECTED);
    }

    @Test
    public void testSubstituteEnvironmentVariableForOnlyVariable() {

        testSubstituteEnvironmentVariable(TEST_ENV_ORIGINAL, TEST_ENV_EXPECTED);
    }

    private void testSubstituteEnvironmentVariable(final String original, final String expected) {

        final String actual = systemPropertiesUtil.substituteEnvironmentVariable(original);

        assertEquals(expected, actual);
    }
}
