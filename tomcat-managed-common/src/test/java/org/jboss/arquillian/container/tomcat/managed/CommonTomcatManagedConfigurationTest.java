package org.jboss.arquillian.container.tomcat.managed;

import static org.junit.Assert.*;

import org.junit.Test;

public class CommonTomcatManagedConfigurationTest
{
   @Test
   public void testGetJavaHomeForDefault()
   {
      final CommonTomcatManagedConfiguration commonTomcatManagedConfiguration = new CommonTomcatManagedConfiguration();

      final String expectedJavaHome = System.getProperty(CommonTomcatManagedConfiguration.JAVA_HOME_SYSTEM_PROPERTY);

      final String actualJavaHome = commonTomcatManagedConfiguration.getJavaHome();

      assertEquals(expectedJavaHome, actualJavaHome);
   }

   @Test
   public void testGetJavaHomeForNonDefault()
   {
      final CommonTomcatManagedConfiguration commonTomcatManagedConfiguration = new CommonTomcatManagedConfiguration();

      final String expectedJavaHome = "EXPECTED_JAVA_HOME";

      commonTomcatManagedConfiguration.setJavaHome(expectedJavaHome);

      final String actualJavaHome = commonTomcatManagedConfiguration.getJavaHome();

      assertEquals(expectedJavaHome, actualJavaHome);
   }
}
