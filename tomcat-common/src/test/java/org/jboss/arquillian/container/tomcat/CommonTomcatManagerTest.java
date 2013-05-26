package org.jboss.arquillian.container.tomcat;

import static org.junit.Assert.*;

import org.junit.Test;

public class CommonTomcatManagerTest
{
   private static final CommonTomcatManager<CommonTomcatConfiguration> COMMON_TOMCAT_MANAGER
         = new CommonTomcatManager<CommonTomcatConfiguration>(new CommonTomcatConfiguration());

   @Test
   public void testNormalizeArchiveName()
   {
      final String normalizeArchiveName = COMMON_TOMCAT_MANAGER.normalizeArchiveName("test.war");

      assertEquals("test", normalizeArchiveName);
   }

   @Test
   public void testNormalizeArchiveNameForRootWar()
   {
      final String normalizeArchiveName = COMMON_TOMCAT_MANAGER.normalizeArchiveName("ROOT.war");

      assertEquals("", normalizeArchiveName);
   }

   @Test(expected = IllegalArgumentException.class)
   public void testNormalizeArchiveNameForNull()
   {
      COMMON_TOMCAT_MANAGER.normalizeArchiveName(null);
   }
}
