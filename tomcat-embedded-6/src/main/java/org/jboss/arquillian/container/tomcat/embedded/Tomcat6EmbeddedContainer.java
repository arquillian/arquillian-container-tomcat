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
package org.jboss.arquillian.container.tomcat.embedded;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.catalina.Container;
import org.apache.catalina.Engine;
import org.apache.catalina.Host;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.core.StandardHost;
import org.apache.catalina.loader.WebappLoader;
import org.apache.catalina.startup.Embedded;
import org.apache.catalina.startup.ExpandWar;
import org.jboss.arquillian.container.spi.client.container.DeployableContainer;
import org.jboss.arquillian.container.spi.client.container.DeploymentException;
import org.jboss.arquillian.container.spi.client.container.LifecycleException;
import org.jboss.arquillian.container.spi.client.protocol.ProtocolDescription;
import org.jboss.arquillian.container.spi.client.protocol.metadata.HTTPContext;
import org.jboss.arquillian.container.spi.client.protocol.metadata.ProtocolMetaData;
import org.jboss.arquillian.container.spi.client.protocol.metadata.Servlet;
import org.jboss.arquillian.container.spi.context.annotation.DeploymentScoped;
import org.jboss.arquillian.core.api.InstanceProducer;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.descriptor.api.Descriptor;
import org.jboss.shrinkwrap.tomcat_6.api.ShrinkWrapStandardContext;

/**
 * <p>
 * Arquillian {@link DeployableContainer} implementation for an Embedded Tomcat server; responsible for both lifecycle and
 * deployment operations.
 * </p>
 *
 * <p>
 * Please note that the context path set for the webapp must begin with a forward slash. Otherwise, certain path operations
 * within Tomcat will behave inconsistently. Though it goes without saying, the host name (bindAddress) cannot have a trailing
 * slash for the same reason.
 * </p>
 *
 * @author <a href="mailto:jean.deruelle@gmail.com">Jean Deruelle</a>
 * @author Dan Allen
 * @version $Revision: $
 */
public class Tomcat6EmbeddedContainer implements DeployableContainer<TomcatEmbeddedConfiguration> {

    private static final Logger log = Logger.getLogger(Tomcat6EmbeddedContainer.class.getName());

    private static final String ENV_VAR = "${env.";

    private static final String TMPDIR_SYS_PROP = "java.io.tmpdir";

    /**
     * Tomcat container configuration
     */
    private TomcatEmbeddedConfiguration configuration;

    /**
     * Tomcat embedded
     */
    private Embedded tomcat;

    private Host host;

    private Engine engine;

    private boolean wasStarted;

    private final List<String> failedUndeployments = new ArrayList<String>();

    @Inject
    @DeploymentScoped
    private InstanceProducer<StandardContext> standardContextProducer;

    @Override
    public Class<TomcatEmbeddedConfiguration> getConfigurationClass() {

        return TomcatEmbeddedConfiguration.class;
    }

    @Override
    public ProtocolDescription getDefaultProtocol() {

        return new ProtocolDescription("Servlet 2.5");
    }

    @Override
    public void setup(final TomcatEmbeddedConfiguration configuration) {

        final String serverName = configuration.getServerName();

        if (serverName == null || "".equals(serverName)) {
            configuration.setServerName("arquillian-tomcat-embedded-6");
        }

        this.configuration = configuration;
    }

    @Override
    public void start() throws LifecycleException {

        try {
            startTomcatEmbedded();
        } catch (final Exception e) {
            throw new LifecycleException("Failed to start embedded Tomcat", e);
        }
    }

    @Override
    public void stop() throws LifecycleException {

        try {
            removeFailedUnDeployments();
        } catch (final Exception e) {
            throw new LifecycleException("Could not clean up", e);
        }
        if (wasStarted) {
            try {
                stopTomcatEmbedded();
            } catch (final org.apache.catalina.LifecycleException e) {
                throw new LifecycleException("Failed to stop Tomcat", e);
            }
        }
    }

    @Override
    public ProtocolMetaData deploy(final Archive<?> archive) throws DeploymentException {

        try {
            final StandardContext standardContext = archive.as(ShrinkWrapStandardContext.class);

            if (archive.getName().startsWith("ROOT")) {
                standardContext.setPath("");
            }

            standardContext.addLifecycleListener(new EmbeddedContextConfig());
            standardContext.setUnpackWAR(configuration.isUnpackArchive());
            standardContext.setJ2EEServer("Arquillian-" + UUID.randomUUID().toString());

            // Need to tell TomCat to use TCCL as parent, else the WebContextClassloader will be looking in AppCL
            standardContext.setParentClassLoader(Thread.currentThread().getContextClassLoader());

            if (standardContext.getUnpackWAR()) {
                deleteUnpackedWAR(standardContext);
            }

            // Override the default Tomcat WebappClassLoader, it delegates to System first. Half our testable app is on System
            // classpath.
            final WebappLoader webappLoader = new WebappLoader(standardContext.getParentClassLoader());
            webappLoader.setDelegate(standardContext.getDelegate());
            webappLoader.setLoaderClass(EmbeddedWebappClassLoader.class.getName());
            standardContext.setLoader(webappLoader);

            host.addChild(standardContext);

            standardContextProducer.set(standardContext);

            final String contextPath = standardContext.getPath();
            final HTTPContext httpContext = new HTTPContext(configuration.getBindAddress(), configuration.getBindHttpPort());

            for (final String mapping : standardContext.findServletMappings()) {
                httpContext.add(new Servlet(standardContext.findServletMapping(mapping), contextPath));
            }

            return new ProtocolMetaData().addContext(httpContext);
        } catch (final Exception e) {
            throw new DeploymentException("Failed to deploy " + archive.getName(), e);
        }
    }

    @Override
    public void undeploy(final Archive<?> archive) throws DeploymentException {

        final StandardContext standardContext = standardContextProducer.get();
        if (standardContext != null) {
            host.removeChild(standardContext);
            try {
                standardContext.stop();
                standardContext.destroy();
            } catch (final Exception e) {
                log.log(Level.WARNING, "Error on undeployment of " + standardContext.getName(), e);
            }
            if (standardContext.getUnpackWAR()) {
                deleteUnpackedWAR(standardContext);
            }
        }
    }

    @Override
    public void deploy(final Descriptor descriptor) throws DeploymentException {

        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void undeploy(final Descriptor descriptor) throws DeploymentException {

        throw new UnsupportedOperationException("Not implemented");
    }

    protected void startTomcatEmbedded() throws UnknownHostException, org.apache.catalina.LifecycleException {

        // creating the tomcat embedded == service tag in server.xml
        tomcat = new Embedded();
        tomcat.setName(configuration.getServerName());
        // TODO this needs to be a lot more robust
        String tomcatHome = configuration.getTomcatHome();
        File tomcatHomeFile = null;
        if (tomcatHome != null) {
            if (tomcatHome.startsWith(ENV_VAR)) {
                final String sysVar = tomcatHome.substring(ENV_VAR.length(), tomcatHome.length() - 1);
                tomcatHome = System.getProperty(sysVar);
                if (tomcatHome != null && tomcatHome.length() > 0 && new File(tomcatHome).isAbsolute()) {
                    tomcatHomeFile = new File(tomcatHome);
                    log.info("Using tomcat home from environment variable: " + tomcatHome);
                }
            } else {
                tomcatHomeFile = new File(tomcatHome);
            }
        }

        if (tomcatHomeFile == null) {
            tomcatHomeFile = new File(System.getProperty(TMPDIR_SYS_PROP), "tomcat-embedded-6");
        }

        tomcatHomeFile.mkdirs();
        tomcat.setCatalinaBase(tomcatHomeFile.getAbsolutePath());
        tomcat.setCatalinaHome(tomcatHomeFile.getAbsolutePath());

        // creates the engine, i.e., <engine> element in server.xml
        engine = tomcat.createEngine();
        engine.setName(configuration.getServerName());
        engine.setDefaultHost(configuration.getBindAddress());
        engine.setService(tomcat);
        tomcat.setContainer(engine);
        tomcat.addEngine(engine);

        // creates the host, i.e., <host> element in server.xml
        final File appBaseFile = new File(tomcatHomeFile, configuration.getAppBase());
        appBaseFile.mkdirs();
        host = tomcat.createHost(configuration.getBindAddress(), appBaseFile.getAbsolutePath());
        if (configuration.getTomcatWorkDir() != null) {
            ((StandardHost) host).setWorkDir(configuration.getTomcatWorkDir());
        }
        ((StandardHost) host).setUnpackWARs(configuration.isUnpackArchive());
        engine.addChild(host);

        // creates an http connector, i.e., <connector> element in server.xml
        final Connector connector =
            tomcat.createConnector(InetAddress.getByName(configuration.getBindAddress()), configuration.getBindHttpPort(),
                false);
        tomcat.addConnector(connector);
        connector.setContainer(engine);

        // starts embedded tomcat
        tomcat.init();
        tomcat.start();
        wasStarted = true;
    }

    protected void stopTomcatEmbedded() throws org.apache.catalina.LifecycleException {

        tomcat.stop();
        tomcat.destroy();
    }

    /**
     * Make sure an the unpacked WAR is not left behind you would think Tomcat would cleanup an unpacked WAR, but it doesn't
     */
    protected void deleteUnpackedWAR(final StandardContext standardContext) {

        String path = standardContext.getPath();

        if ("".equals(path)) {
            path = "/ROOT";
        }

        final File unpackDir = new File(host.getAppBase(), path.substring(1));

        if (unpackDir.exists()) {
            ExpandWar.deleteDir(unpackDir);
        }
    }

    private void undeploy(final String name) throws DeploymentException {

        final Container child = host.findChild(name);
        if (child != null) {
            host.removeChild(child);
        }
    }

    private void removeFailedUnDeployments() throws IOException {

        final List<String> remainingDeployments = new ArrayList<String>();
        for (final String name : failedUndeployments) {
            try {
                undeploy(name);
            } catch (final Exception e) {
                final IOException ioe = new IOException();
                ioe.initCause(e);
                throw ioe;
            }
        }
        if (remainingDeployments.size() > 0) {
            log.severe("Failed to undeploy these artifacts: " + remainingDeployments);
        }
        failedUndeployments.clear();
    }
}
