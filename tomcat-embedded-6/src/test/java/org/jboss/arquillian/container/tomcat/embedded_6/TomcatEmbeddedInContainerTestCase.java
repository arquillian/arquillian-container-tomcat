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
package org.jboss.arquillian.container.tomcat.embedded_6;

import java.io.InputStream;
import java.net.URL;

import javax.annotation.Resource;
import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.tomcat.test.TestBean;
import org.jboss.arquillian.container.tomcat.test.TestServlet;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests that Tomcat deployments into the Tomcat server work through the
 * Arquillian lifecycle
 *
 * @author Dan Allen
 * @version $Revision: $
 */
@RunWith(Arquillian.class)
public class TomcatEmbeddedInContainerTestCase
{
   private static final String HELLO_WORLD_URL = "http://localhost:8888/test2/Test";

   @Resource(name = "resourceInjectionTestName")
   private String resourceInjectionTestValue;

   @Inject
   TestBean testBean;

   @Deployment
   public static WebArchive createTestArchive()
   {
      return ShrinkWrap
            .create(WebArchive.class, "test2.war")
            .addClasses(TestServlet.class, TestBean.class)
            .addAsLibraries(
                  Maven.configureResolver().workOffline().loadPomFromFile("pom.xml")
                        .resolve("org.jboss.weld.servlet:weld-servlet").withTransitivity().asFile())
            .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml").setWebXML("in-container-web.xml");
   }

   @Test
   public void shouldBeAbleToInjectMembersIntoTestClass()
   {
      Assert.assertEquals("Tomcat", resourceInjectionTestValue);
      Assert.assertNotNull(testBean);
      Assert.assertEquals("Tomcat", testBean.getName());
   }

   @Test
   public void shouldBeAbleToInvokeServletInDeployedWebApp() throws Exception
   {
      final String expected = "hello";

      final URL url = new URL(HELLO_WORLD_URL);
      final InputStream in = url.openConnection().getInputStream();

      final byte[] buffer = new byte[10000];
      final int len = in.read(buffer);
      String httpResponse = "";
      for (int q = 0; q < len; q++)
      {
         httpResponse += (char) buffer[q];
      }

      Assert.assertEquals("Expected output was not equal by value", expected, httpResponse);
   }
}
