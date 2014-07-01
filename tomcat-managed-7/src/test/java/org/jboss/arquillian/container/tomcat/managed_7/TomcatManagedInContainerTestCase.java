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

import java.io.InputStream;
import java.net.URL;
import java.util.logging.Logger;
import javax.annotation.Resource;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.container.tomcat.managed.MyBean;
import org.jboss.arquillian.container.tomcat.managed.MyServlet;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.asset.IOUtilDelegator;
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
public class TomcatManagedInContainerTestCase
{
    private static final Logger log = Logger.getLogger(TomcatManagedInContainerTestCase.class.getName());

    /**
     * Define the deployment
     */
    @Deployment
    public static WebArchive createTestArchive()
    {
        WebArchive war = ShrinkWrap.create(WebArchive.class, "test2.war")
                                .addClasses(MyServlet.class, MyBean.class)
                                   .addAsLibraries(
                                           Maven.configureResolver()
                                               .workOffline()
                                               .loadPomFromFile("pom.xml")
                                               .resolve("org.jboss.weld.servlet:weld-servlet")
                                               .withTransitivity()
                                               .asFile())
                                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
                                .setWebXML("in-container-web.xml");
        /// DEBUG - see what's
        //war.as(ZipExporter.class).exportTo( new File("/tmp/arq.zip"), true );
        return war;
    }

    // -------------------------------------------------------------------------------------||
    // Tests -------------------------------------------------------------------------------||
    // -------------------------------------------------------------------------------------||

    @Resource(name = "resourceInjectionTestName") private String resourceInjectionTestValue;

    /**
     * Ensures the {@link MyServlet} returns the expected response
     */
    @Test
    public void shouldBeAbleToInjectMembersIntoTestClass(MyBean testBean)
    {
      log.info("Name: " + this.resourceInjectionTestValue);
      Assert.assertEquals("Hello World from an evn-entry", this.resourceInjectionTestValue);
      Assert.assertNotNull(testBean);
      Assert.assertEquals("Hello World from an evn-entry", testBean.getName());
    }

   @Test @RunAsClient
   public void shouldBeAbleToInvokeServletInDeployedWebApp(@ArquillianResource URL contextRoot) throws Exception
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
