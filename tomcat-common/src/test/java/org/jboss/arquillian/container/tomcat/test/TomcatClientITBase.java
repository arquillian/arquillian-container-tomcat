/*
 * JBoss, Home of Professional Open Source
 * Copyright 2014, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.arquillian.container.tomcat.test;

import static org.junit.Assert.assertEquals;

import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

public abstract class TomcatClientITBase {

    protected static final TestDeploymentFactory TEST_DEPLOYMENT_FACTORY = new TestDeploymentFactory();

    private static final String TEST_JSP_RESPONSE = "welcome";

    private static final String TEST_SERVLET_RESPONSE = "hello";

    @Test
    @OperateOnDeployment(TestDeploymentFactory.ROOT_CONTEXT)
    public void shouldBeAbleToInvokeServletInDeployedRootWebApp(@ArquillianResource final URL contextRoot)
        throws Exception {

        final URL servletUrl = getServletUrl(contextRoot);

        testDeployment(servletUrl, TEST_SERVLET_RESPONSE);
    }

    @Test
    @OperateOnDeployment(TestDeploymentFactory.TEST_CONTEXT)
    public void shouldBeAbleToInvokeServletInDeployedWebApp(@ArquillianResource final URL contextRoot) throws Exception {

        final URL servletUrl = getServletUrl(contextRoot);

        testDeployment(servletUrl, TEST_SERVLET_RESPONSE);
    }

    @Test
    @OperateOnDeployment(TestDeploymentFactory.ROOT_CONTEXT)
    public void shouldBeAbleToInvokeJspInDeployedRootWebApp(@ArquillianResource final URL contextRoot) throws Exception {

        testDeployment(contextRoot, TEST_JSP_RESPONSE);
    }

    @Test
    @OperateOnDeployment(TestDeploymentFactory.TEST_CONTEXT)
    public void shouldBeAbleToInvokeJspInDeployedWebApp(@ArquillianResource final URL contextRoot) throws Exception {

        testDeployment(contextRoot, TEST_JSP_RESPONSE);
    }

    private URL getServletUrl(final URL contextRoot) throws MalformedURLException {

        return new URL(contextRoot, TestDeploymentFactory.TEST_SERVLET_PATH);
    }

    private void testDeployment(final URL url, final String expected) throws MalformedURLException, IOException {

        final String httpResponse = getHttpResponse(url);

        assertEquals("Expected output was not equal by value", expected, httpResponse);
    }

    private String getHttpResponse(final URL url) throws IOException {

        final InputStream in = url.openConnection().getInputStream();

        final byte[] buffer = new byte[10000];
        final int len = in.read(buffer);
        String httpResponse = "";
        for (int q = 0; q < len; q++) {
            httpResponse += (char) buffer[q];
        }
        return httpResponse.replaceAll("\\s+", "");
    }
}
