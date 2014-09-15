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
package org.jboss.arquillian.container.tomcat.managed;

import static org.jboss.arquillian.container.tomcat.managed.TestDeploymentFactory.*;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.asset.IOUtilDelegator;
import org.junit.Assert;
import org.junit.Test;

public abstract class TomcatManagedClientITBase
{
    protected static final TestDeploymentFactory TEST_DEPLOYMENT_FACTORY = new TestDeploymentFactory();

    @Test
    @OperateOnDeployment(ROOT_CONTEXT)
    public void shouldBeAbleToInvokeServletInDeployedRootWebApp(@ArquillianResource final URL contextRoot)
        throws Exception
    {
        testDeployment(contextRoot);
    }

    @Test
    @OperateOnDeployment(TEST_CONTEXT)
    public void shouldBeAbleToInvokeServletInDeployedWebApp(@ArquillianResource final URL contextRoot) throws Exception
    {
        testDeployment(contextRoot);
    }

    private void testDeployment(final URL contextRoot) throws MalformedURLException, IOException
    {
        final String expected = "hello";

        final URL url = new URL(contextRoot, "Test");
        final InputStream in = url.openConnection().getInputStream();

        final byte[] buffer = IOUtilDelegator.asByteArray(in);
        final String httpResponse = new String(buffer);

        Assert.assertEquals("Expected output was not equal by value", expected, httpResponse);
    }
}