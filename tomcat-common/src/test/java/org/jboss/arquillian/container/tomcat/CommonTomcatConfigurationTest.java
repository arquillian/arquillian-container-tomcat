/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 * See the copyright.txt in the distribution for a
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
package org.jboss.arquillian.container.tomcat;

import static org.junit.Assert.*;

import java.net.URI;
import java.net.URL;

import org.jboss.arquillian.container.spi.ConfigurationException;
import org.junit.Before;
import org.junit.Test;

public class CommonTomcatConfigurationTest
{
    private TomcatConfiguration commonTomcatConfiguration;

    @Before
    public void setUp()
    {
        commonTomcatConfiguration = new TomcatConfiguration();
    }

    @Test
    public void testCreateJmxUriForDefaultBindAddressAndJmxServerPort()
    {
        final URI actualJmxUri = commonTomcatConfiguration.createJmxUri();

        final String actualJmxUriString = actualJmxUri.toString();

        final String expectedJmxUriString = "service:jmx:rmi:///jndi/rmi://localhost:8089/jmxrmi";

        assertEquals(expectedJmxUriString, actualJmxUriString);
    }

    @Test
    public void testCreateJmxUriForSetBindAddressAndJmxServerPort()
    {
        final int testJmxServerPort = 5;
        final String testBindAddress = "somewhere";

        commonTomcatConfiguration.setBindAddress(testBindAddress);
        commonTomcatConfiguration.setJmxPort(testJmxServerPort);

        final URI actualJmxUri = commonTomcatConfiguration.createJmxUri();

        final String actualJmxUriString = actualJmxUri.toString();

        final String expectedJmxUriString = "service:jmx:rmi:///jndi/rmi://" + testBindAddress + ":" + testJmxServerPort
            + "/jmxrmi";

        assertEquals(expectedJmxUriString, actualJmxUriString);
    }

    @Test(expected = ConfigurationException.class)
    public void testCreateJmxUriForInvalidUri()
    {
        commonTomcatConfiguration.setBindAddress("^");

        commonTomcatConfiguration.createJmxUri();
    }

    @Test
    public void testCreateManagerUrlForDefaultHostAndPort()
    {
        final URL actualManagerUrl = commonTomcatConfiguration.createManagerUrl();

        final String actualManagerUrlString = actualManagerUrl.toString();

        final String expectedManagerUrlString = "http://localhost:8080/manager";

        assertEquals(expectedManagerUrlString, actualManagerUrlString);
    }

    @Test
    public void testCreateManagerUrlForSetHostAndPort()
    {
        final String testBindAddress = "somewhere";
        final int testBindHttpPort = 5;

        commonTomcatConfiguration.setBindAddress(testBindAddress);
        commonTomcatConfiguration.setBindHttpPort(testBindHttpPort);

        final URL actualManagerUrl = commonTomcatConfiguration.createManagerUrl();

        final String actualManagerUrlString = actualManagerUrl.toString();

        final String expectedManagerUrlString = "http://" + testBindAddress + ":" + testBindHttpPort + "/manager";

        assertEquals(expectedManagerUrlString, actualManagerUrlString);
    }

    @Test(expected = ConfigurationException.class)
    public void testCreateManagerUrlForInvalidUrl()
    {
        commonTomcatConfiguration.setBindAddress(":");

        commonTomcatConfiguration.createManagerUrl();
    }

}
