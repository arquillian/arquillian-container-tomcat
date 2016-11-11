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

import static org.jboss.arquillian.container.tomcat.test.TestDeploymentFactory.ROOT_CONTEXT;
import static org.jboss.arquillian.container.tomcat.test.TestDeploymentFactory.SERVLET_2_4;
import static org.jboss.arquillian.container.tomcat.test.TestDeploymentFactory.TEST_CONTEXT;
import static org.junit.Assert.assertEquals;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.tomcat.test.TestBean;
import org.jboss.arquillian.container.tomcat.test.TestDeploymentFactory;
import org.jboss.arquillian.container.tomcat.test.TestServlet;
import org.jboss.arquillian.container.tomcat.test.TomcatClientITBase;
import org.jboss.arquillian.container.tomcat.test.TomcatInContainerITBase;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.runner.RunWith;

/**
 * Tests that Tomcat deployments into the Tomcat server work through the Arquillian lifecycle
 *
 * @author Dan Allen
 * @version $Revision: $
 */
@RunWith(Arquillian.class)
public class Tomcat55ManagedInContainerIT extends TomcatInContainerITBase {

    @Deployment(name = ROOT_CONTEXT)
    public static WebArchive createRootDeployment() {

        return createDeployment(ROOT_CONTEXT);
    }

    @Deployment(name = TEST_CONTEXT)
    public static WebArchive createTestDeployment() {

        return createDeployment(TEST_CONTEXT);
    }

    @Override
    protected void assertInjection(final TestBean testBean) {

        assertEquals("Hello World from an evn-entry", resourceInjectionTestValue);
    }

    private static WebArchive createDeployment(final String context) {

        final String archiveName = getArchiveName(context);

        final WebArchive war =
            ShrinkWrap
                .create(WebArchive.class, archiveName)
                .addClasses(TestServlet.class, TestBean.class, TomcatClientITBase.class, TomcatInContainerITBase.class,
                    TestDeploymentFactory.class).addAsResource("logging.properties")
                .setWebXML("in-container-web-" + SERVLET_2_4 + ".xml");

        return war;
    }

    private static String getArchiveName(final String context) {

        return context + ".war";
    }
}
