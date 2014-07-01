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
package org.jboss.arquillian.container.tomcat.managed_7;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Logger;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.container.tomcat.managed.MyServlet;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.IOUtilDelegator;
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

   private static final String ROOT_CONTEXT = "ROOT";

   private static final String TEST_CONTEXT = "test";

   // -------------------------------------------------------------------------------------||
   // Instance Members --------------------------------------------------------------------||
   // -------------------------------------------------------------------------------------||

   /**
    * Define the ROOT context deployment
    */
   @Deployment(name=ROOT_CONTEXT, testable = false)
   public static WebArchive createRootDeployment()
   {
      final String archiveName = ROOT_CONTEXT + ".war";

      return createDeployment(archiveName);
   }

   /**
    * Define the test context deployment
    */
   @Deployment(name=TEST_CONTEXT, testable = false)
   public static WebArchive createTestDeployment()
   {
      final String archiveName = TEST_CONTEXT + ".war";

      return createDeployment(archiveName);
   }

   private static WebArchive createDeployment(final String archiveName)
   {
      return ShrinkWrap
            .create(WebArchive.class, archiveName)
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
    * Ensures the {@link MyServlet} returns the expected response for the ROOT context
    */
   @Test
   @OperateOnDeployment(ROOT_CONTEXT)
   public void shouldBeAbleToInvokeServletInDeployedRootWebApp(@ArquillianResource URL contextRoot) throws Exception
   {
      testDeployment(contextRoot);
   }

   /**
    * Ensures the {@link MyServlet} returns the expected response for the test context
    */
   @Test
   @OperateOnDeployment(TEST_CONTEXT)
   public void shouldBeAbleToInvokeServletInDeployedWebApp(@ArquillianResource URL contextRoot) throws Exception
   {
      testDeployment(contextRoot);
   }

   private void testDeployment(final URL contextRoot) throws MalformedURLException, IOException
   {
      // Define the input and expected outcome
      final String expected = "hello";

      URL url = new URL(contextRoot, "Test");
      InputStream in = url.openConnection().getInputStream();

       byte[] buffer = IOUtilDelegator.asByteArray(in);
       String httpResponse = new String(buffer);

      // Test
      Assert.assertEquals("Expected output was not equal by value", expected, httpResponse);
      log.info("Got expected result from Http Servlet: " + httpResponse);
   }
}
