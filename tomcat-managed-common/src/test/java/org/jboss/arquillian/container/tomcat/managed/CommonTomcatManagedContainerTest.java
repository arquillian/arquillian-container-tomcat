package org.jboss.arquillian.container.tomcat.managed;

import static org.junit.Assert.*;

import java.io.File;

import org.jboss.arquillian.container.spi.client.protocol.ProtocolDescription;
import org.junit.Before;
import org.junit.Test;

public class CommonTomcatManagedContainerTest
{

   private CommonTomcatManagedContainer commonTomcatManagedContainer;

   @Before
   public void setUp()
   {
      commonTomcatManagedContainer = new CommonTomcatManagedContainer()
      {
         @Override
         public ProtocolDescription getDefaultProtocol()
         {
            return null;
         }
      };
   }

   @Test
   public void testGetJavaCommandForConfiguration()
   {
      final CommonTomcatManagedConfiguration commonTomcatManagedConfiguration = new CommonTomcatManagedConfiguration();

      final String testJavaHome = File.separator + "test" + File.separator + "java" + File.separator + "home";

      commonTomcatManagedConfiguration.setJavaHome(testJavaHome);

      commonTomcatManagedContainer.setup(commonTomcatManagedConfiguration);

      final String actualJavaCommand = commonTomcatManagedContainer.getJavaCommand();

      final String expectedJavaCommand = testJavaHome + File.separator + "bin" + File.separator + "java";

      assertEquals(expectedJavaCommand, actualJavaCommand);
   }

   @Test(expected = IllegalStateException.class)
   public void testGetJavaCommandForNoConfiguration()
   {
      // No .setup(commonTomcatManagedConfiguration)

      commonTomcatManagedContainer.getJavaCommand();
   }
}
