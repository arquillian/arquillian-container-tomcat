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
import static org.jboss.arquillian.container.tomcat.test.TestDeploymentFactory.SERVLET_3_1;
import static org.jboss.arquillian.container.tomcat.test.TestDeploymentFactory.TEST_CONTEXT;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.tomcat.test.TomcatClientITBase;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.runner.RunWith;

/**
 * Tests that Tomcat deployments into the Tomcat 8 server work through the Arquillian lifecycle
 *
 * @author <a href="mailto:jean.deruelle@gmail.com">Jean Deruelle</a>
 * @author Dan Allen
 * @author <a href="mailto:dadrus@gmx.de">Dimitrij Drus</a>
 * @version $Revision: $
 *
 */
@RunWith(Arquillian.class)
public class Tomcat8ManagedClientIT extends TomcatClientITBase {

    @Deployment(name = ROOT_CONTEXT, testable = false)
    public static WebArchive createRootDeployment() {

        return TEST_DEPLOYMENT_FACTORY.createWebAppClientDeployment(ROOT_CONTEXT, SERVLET_3_1);
    }

    @Deployment(name = TEST_CONTEXT, testable = false)
    public static WebArchive createTestDeployment() {

        return TEST_DEPLOYMENT_FACTORY.createWebAppClientDeployment(TEST_CONTEXT, SERVLET_3_1);
    }
}
