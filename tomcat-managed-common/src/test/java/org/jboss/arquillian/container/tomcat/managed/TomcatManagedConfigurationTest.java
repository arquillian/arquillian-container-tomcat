package org.jboss.arquillian.container.tomcat.managed;

import static org.junit.Assert.*;

import org.junit.Test;

public class TomcatManagedConfigurationTest {

    @Test
    public void testGetJavaHomeForDefault() {

        final TomcatManagedConfiguration commonTomcatManagedConfiguration = new TomcatManagedConfiguration();

        final String expectedJavaHome = System.getProperty(TomcatManagedConfiguration.JAVA_HOME_SYSTEM_PROPERTY);

        final String actualJavaHome = commonTomcatManagedConfiguration.getJavaHome();

        assertEquals(expectedJavaHome, actualJavaHome);
    }

    @Test
    public void testGetJavaHomeForNonDefault() {

        final TomcatManagedConfiguration commonTomcatManagedConfiguration = new TomcatManagedConfiguration();

        final String expectedJavaHome = "EXPECTED_JAVA_HOME";

        commonTomcatManagedConfiguration.setJavaHome(expectedJavaHome);

        final String actualJavaHome = commonTomcatManagedConfiguration.getJavaHome();

        assertEquals(expectedJavaHome, actualJavaHome);
    }
}
