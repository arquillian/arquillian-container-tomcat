/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.arquillian.container.tomcat.managed_6;

import java.io.InputStream;
import java.net.URL;
import java.util.logging.Logger;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.descriptor.api.Descriptors;
import org.jboss.shrinkwrap.descriptor.api.webapp30.WebAppDescriptor;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests that Tomcat deployments into the Tomcat server work through the
 * Arquillian lifecycle
 * 
 * @author <a href="mailto:jean.deruelle@gmail.com">Jean Deruelle</a>
 * @author Dan Allen
 * @version $Revision: $
 * 
 */
@RunWith(Arquillian.class)
public class TomcatManagedClientTestCase
{
   // -------------------------------------------------------------------------------------||
   // Class Members -----------------------------------------------------------------------||
   // -------------------------------------------------------------------------------------||

   /**
    * Logger
    */
   private static final Logger log = Logger.getLogger(TomcatManagedClientTestCase.class.getName());

   // -------------------------------------------------------------------------------------||
   // Instance Members --------------------------------------------------------------------||
   // -------------------------------------------------------------------------------------||

   /**
    * Define the deployment
    */
   @Deployment(testable = false)
   public static WebArchive createDeployment()
   {
      return ShrinkWrap
            .create(WebArchive.class, "test.war")
            .addClass(MyServlet.class)
            .setWebXML(
                    new StringAsset(Descriptors.create(WebAppDescriptor.class).version("2.5")
                            .createServlet()
                                .servletClass(MyServlet.class.getName())
                                .servletName("MyServlet").up()
                            .createServletMapping()
                                .servletName("MyServlet")
                                .urlPattern("/Test").up()
                          .exportAsString()));
   }

   // -------------------------------------------------------------------------------------||
   // Tests -------------------------------------------------------------------------------||
   // -------------------------------------------------------------------------------------||

   /**
    * Ensures the {@link HelloWorldServlet} returns the expected response
    */
   @Test
   public void shouldBeAbleToInvokeServletInDeployedWebApp(@ArquillianResource URL contextRoot) throws Exception
   {
      // Define the input and expected outcome
      final String expected = "hello";

      URL url = new URL(contextRoot, "Test");
      InputStream in = url.openConnection().getInputStream();

      byte[] buffer = new byte[10000];
      int len = in.read(buffer);
      String httpResponse = "";
      for (int q = 0; q < len; q++)
         httpResponse += (char) buffer[q];

      // Test
      Assert.assertEquals("Expected output was not equal by value", expected, httpResponse);
      log.info("Got expected result from Http Servlet: " + httpResponse);
   }
}
