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

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;

/**
 * @author Radoslav Husar
 */
public final class TestDeploymentFactory {

    public static final String TEST_SERVLET_PATH = "/Test";

    public static final String TEST_WELCOME_FILE = "index.jsp";

    public static final String SERVLET_5_0 = "5.0";

    public static final String ROOT_CONTEXT = "ROOT";

    public static final String TEST_CONTEXT = "test";

    /**
     * Tomcat reads a "#" in a deployment name as a context path separator, so this deploys to "/foo/bar".
     */
    public static final String MULTI_SEGMENT_CONTEXT = "foo#bar";

    /**
     * The context path {@link #MULTI_SEGMENT_CONTEXT} is expected to be deployed to.
     */
    public static final String MULTI_SEGMENT_CONTEXT_PATH = "/foo/bar";

    /**
     * Tomcat reads a "##" in a deployment name as the start of a parallel deployment version, so this deploys to
     * "/baz" as version "1.0".
     */
    public static final String VERSIONED_CONTEXT = "baz##1.0";

    /**
     * The context path {@link #VERSIONED_CONTEXT} is expected to be deployed to; a versioned deployment is served
     * from its context path, not from its versioned context name.
     */
    public static final String VERSIONED_CONTEXT_PATH = "/baz";

    public WebArchive createWebAppClientDeployment(final String contextRoot, final String webAppVersion) {

        final String archiveName = getArchiveName(contextRoot);

        return ShrinkWrap.create(WebArchive.class, archiveName)
            .addClass(TestServlet.class)
            .addAsResource("logging.properties")
            .addAsWebResource(TEST_WELCOME_FILE)
            .setWebXML("web-" + webAppVersion + ".xml");
    }

    public WebArchive createWebAppInContainerDeployment(final String contextRoot, final String webAppVersion) {

        final String archiveName = getArchiveName(contextRoot);

        return ShrinkWrap
            .create(WebArchive.class, archiveName)
            .addClasses(TestServlet.class, TestBean.class, TomcatInContainerITBase.class, this.getClass())
            .addAsResource("logging.properties")
            .addAsLibraries(
                Maven.configureResolver().workOffline().loadPomFromFile("pom.xml")
                    .resolve("org.jboss.weld.servlet:weld-servlet-core").withTransitivity().asFile())
            .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
            .setWebXML("in-container-web-" + webAppVersion + ".xml");
    }

    private String getArchiveName(final String contextRoot) {

        final String archiveName = contextRoot + ".war";

        return archiveName;
    }
}
