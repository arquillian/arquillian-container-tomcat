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

import org.jboss.arquillian.container.tomcat.test.TestBean;
import org.jboss.arquillian.container.tomcat.test.TestServlet;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.descriptor.api.Descriptors;
import org.jboss.shrinkwrap.descriptor.api.webapp30.WebAppDescriptor;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;

public final class TestDeploymentFactory
{
    public static final String TEST_SERVLET_PATH = "/Test";

    public static final Class<?> TEST_SERVLET_CLASS = TestServlet.class;

    public static final String TEST_SERVLET_CLASS_NAME = TEST_SERVLET_CLASS.getName();

    public static final String TEST_SERVLET_NAME = TEST_SERVLET_CLASS.getSimpleName();

    protected static final String SERVLET_2_4 = "2.4";

    protected static final String SERVLET_2_5 = "2.5";

    protected static final String SERVLET_3_0 = "3.0";

    protected static final String ROOT_CONTEXT = "ROOT";

    protected static final String TEST_CONTEXT = "test";

    public WebArchive createWebAppClientDeployment(final String contextRoot, final String webAppVersion)
    {
        final String archiveName = getArchiveName(contextRoot);

        final StringAsset webAppDescriptor = new StringAsset(Descriptors.create(WebAppDescriptor.class)
            .version(webAppVersion).createServlet().servletClass(TEST_SERVLET_CLASS_NAME)
            .servletName(TEST_SERVLET_NAME).up().createServletMapping().servletName(TEST_SERVLET_NAME)
            .urlPattern(TEST_SERVLET_PATH).up().exportAsString());

        final WebArchive war = ShrinkWrap.create(WebArchive.class, archiveName).addClass(TestServlet.class)
            .setWebXML(webAppDescriptor);

        return war;
    }

    public WebArchive createWebAppInContainerDeployment(final String contextRoot, final String webAppVersion)
    {
        final String archiveName = getArchiveName(contextRoot);

        final WebArchive war = ShrinkWrap
            .create(WebArchive.class, archiveName)
            .addClasses(TestServlet.class, TestBean.class, TomcatManagedInContainerITBase.class, this.getClass())
            .addAsLibraries(
                Maven.configureResolver().workOffline().loadPomFromFile("pom.xml")
                    .resolve("org.jboss.weld.servlet:weld-servlet").withTransitivity().asFile())
            .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml").setWebXML("in-container-web.xml");

        return war;
    }

    private String getArchiveName(final String contextRoot)
    {
        final String archiveName = contextRoot + ".war";

        return archiveName;
    }
}
