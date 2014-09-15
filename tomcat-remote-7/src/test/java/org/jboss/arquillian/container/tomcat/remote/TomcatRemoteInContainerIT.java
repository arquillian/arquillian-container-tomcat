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
package org.jboss.arquillian.container.tomcat.remote;

import java.io.InputStream;
import java.net.URL;

import javax.annotation.Resource;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.container.tomcat.test.TestBean;
import org.jboss.arquillian.container.tomcat.test.TestServlet;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests that Tomcat deployments into the Tomcat server work through the Arquillian lifecycle
 *
 * @author Dan Allen
 * @version $Revision: $
 */
@RunWith(Arquillian.class)
public class TomcatRemoteInContainerIT
{
    @Resource(name = "resourceInjectionTestName")
    private String resourceInjectionTestValue;

    @Deployment
    public static WebArchive createTestArchive()
    {
        final WebArchive war = ShrinkWrap
            .create(WebArchive.class, "test2.war")
            .addClasses(TestServlet.class, TestBean.class)
            .addAsLibraries(
                Maven.configureResolver().workOffline().loadPomFromFile("pom.xml")
                    .resolve("org.jboss.weld.servlet:weld-servlet").withTransitivity().asFile())
            .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml").setWebXML("in-container-web.xml");

        return war;
    }

    @Test
    public void shouldBeAbleToInjectMembersIntoTestClass(final TestBean testBean)
    {
        Assert.assertEquals("Hello World from an evn-entry", this.resourceInjectionTestValue);
        Assert.assertNotNull(testBean);
        Assert.assertEquals("Hello World from an evn-entry", testBean.getName());
    }

    @Test
    @RunAsClient
    public void shouldBeAbleToInvokeServletInDeployedWebApp(@ArquillianResource final URL contextRoot) throws Exception
    {
        final String expected = "hello";

        final URL url = new URL(contextRoot, "Test");
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
