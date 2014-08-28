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
package org.jboss.arquillian.container.tomcat.embedded_7;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
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
 * @author <a href="mailto:ian@ianbrandt.com">Ian Brandt</a>
 * @version $Revision: $
 */
@RunWith(Arquillian.class)
public class TomcatEmbeddedClientTestCase
{
   // -------------------------------------------------------------------------------------||
   // Class Members -----------------------------------------------------------------------||
   // -------------------------------------------------------------------------------------||

   private static final String ROOT_CONTEXT = "ROOT";

   private static final String TEST_CONTEXT = "test";

   private static final String TEST_SERVLET = "Test";

   private static final String TEST_WELCOME_FILE = "index.jsp";

   // -------------------------------------------------------------------------------------||
   // Instance Members --------------------------------------------------------------------||
   // -------------------------------------------------------------------------------------||

   /**
    * Define the root context deployment
    */
   @Deployment(name = ROOT_CONTEXT, testable = false)
   public static WebArchive createRootDeployment()
   {
      return createDeployment(getWarName(ROOT_CONTEXT));
   }

   /**
    * Define the test context deployment
    */
   @Deployment(name = TEST_CONTEXT, testable = false)
   public static WebArchive createTestDeployment()
   {
      return createDeployment(getWarName(TEST_CONTEXT));
   }

   private static String getWarName(final String contextName)
   {
      return contextName + ".war";
   }

   private static WebArchive createDeployment(final String archiveName)
   {
      return ShrinkWrap
            .create(WebArchive.class, archiveName)
            .addClass(MyServlet.class)
            .addAsWebResource(TEST_WELCOME_FILE)
            .setWebXML(
                  new StringAsset(Descriptors.create(WebAppDescriptor.class).version("3.0").createServlet()
                        .servletClass(MyServlet.class.getName()).servletName("MyServlet").up().createServletMapping()
                        .servletName("MyServlet").urlPattern("/" + TEST_SERVLET).up().exportAsString()));
   }

   // -------------------------------------------------------------------------------------||
   // Tests -------------------------------------------------------------------------------||
   // -------------------------------------------------------------------------------------||

   /**
    * Ensures the Test Servlet returns the expected response.
    */
   @Test
   @OperateOnDeployment(TEST_CONTEXT)
   public void shouldBeAbleToInvokeServletInDeployedWebApp(@ArquillianResource URL contextURL) throws Exception
   {
      final String expected = "hello";

      URL servletUrl = new URL(contextURL, TEST_SERVLET);
      String httpResponse = getHttpResponse(servletUrl);

      Assert.assertEquals("Expected output was not equal by value", expected, httpResponse);
   }

   /**
    * Ensures the JSP welcome file returns the expected response.
    */
   @Test
   @OperateOnDeployment(ROOT_CONTEXT)
   public void shouldBeAbleToInvokeJspInDeployedWebApp(@ArquillianResource URL contextURL) throws Exception
   {
      final String expected = "welcome";

      String httpResponse = getHttpResponse(contextURL);

      Assert.assertEquals("Expected output was not equal by value", expected, httpResponse);
   }

   private String getHttpResponse(URL servletUrl) throws IOException
   {
      InputStream in = servletUrl.openConnection().getInputStream();

      byte[] buffer = new byte[10000];
      int len = in.read(buffer);
      String httpResponse = "";
      for (int q = 0; q < len; q++)
         httpResponse += (char) buffer[q];
      return httpResponse;
   }
}
