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

import org.apache.catalina.Host;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.core.StandardHost;
import org.apache.catalina.startup.CatalinaProperties;
import org.apache.catalina.startup.ExpandWar;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.util.ContextName;
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
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.descriptor.api.Descriptor;

/**
 * <p>
 * Arquillian {@link DeployableContainer} implementation for an Embedded Tomcat server; responsible for both lifecycle
 * and
 * deployment operations.
 * </p>
 * <p>
 * <p>
 * Please note that the context path set for the webapp must begin with a forward slash. Otherwise, certain path
 * operations
 * within Tomcat will behave inconsistently. Though it goes without saying, the host name (bindAddress) cannot have a
 * trailing
 * slash for the same reason.
 * </p>
 *
 * @author <a href="mailto:jean.deruelle@gmail.com">Jean Deruelle</a>
 * @author Dan Allen
 * @author <a href="mailto:ian@ianbrandt.com">Ian Brandt</a>
 * @version $Revision: $
 * @see <a href='http://svn.apache.org/repos/asf/tomcat/trunk/test/org/apache/catalina/startup/TomcatBaseTest.java'>
 * org.apache.catalina.startup.TomcatBaseTest</a>
 */
public class Tomcat8EmbeddedContainer implements DeployableContainer<TomcatEmbeddedConfiguration> {

    private final SystemPropertiesUtil systemPropertiesUtil = new SystemPropertiesUtil();

    /**
     * Tomcat container configuration
     */
    private TomcatEmbeddedConfiguration configuration;

    /**
     * Tomcat embedded
     */
    private Tomcat tomcat;

    private Host host;

    private EmbeddedHostConfig embeddedHostConfig;

    private File appBase;

    private boolean wasStarted;

    @Inject
    @DeploymentScoped
    private InstanceProducer<StandardContext> standardContextProducer;

    @Override
    public Class<TomcatEmbeddedConfiguration> getConfigurationClass() {

        return TomcatEmbeddedConfiguration.class;
    }

    @Override
    public ProtocolDescription getDefaultProtocol() {

        return new ProtocolDescription("Servlet 3.0");
    }

    @Override
    public void setup(final TomcatEmbeddedConfiguration configuration) {

        final String serverName = configuration.getServerName();

        if (serverName == null || "".equals(serverName)) {
            configuration.setServerName("arquillian-tomcat-embedded-8");
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
            // Ensure we don't create a corrupted archive by exporting to a file that already exists.
            deleteWar(archive);

            final File archiveFile = new File(appBase, archive.getName());
            archive.as(ZipExporter.class).exportTo(archiveFile, true);

            embeddedHostConfig.deployWAR(archive.getName());

            final ContextName contextName = getContextName(archive);
            final StandardContext standardContext = (StandardContext) host.findChild(contextName.getName());
            standardContextProducer.set(standardContext);

            final HTTPContext httpContext =
                new HTTPContext(configuration.getBindAddress(), configuration.getBindHttpPort());

            for (final String mapping : standardContext.findServletMappings()) {
                httpContext.add(new Servlet(standardContext.findServletMapping(mapping), contextName.getPath()));
            }

            return new ProtocolMetaData().addContext(httpContext);
        } catch (final Exception e) {
            throw new DeploymentException("Failed to deploy " + archive.getName(), e);
        }
    }

    @Override
    public void undeploy(final Archive<?> archive) throws DeploymentException {

        try {
            embeddedHostConfig.undeployWAR(archive.getName());

            deleteWar(archive);
        } catch (final Exception e) {
            throw new DeploymentException("Failed to undeploy " + archive.getName(), e);
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

    protected void startTomcatEmbedded() throws LifecycleException, org.apache.catalina.LifecycleException {

        /*
         * Derived from setUp() in
         * http://svn.apache.org/repos/asf/tomcat/tc7.0.x/tags/TOMCAT_7_0_16/test/org/apache/catalina/startup
         * /TomcatBaseTest.java.
         */
        final File tempDir = getTomcatHomeFile();

        System.setProperty("catalina.base", tempDir.getAbsolutePath());
        // Trigger loading of catalina.properties.
        CatalinaProperties.getProperty("foo");

        appBase = new File(tempDir, "webapps");
        if (!appBase.exists() && !appBase.mkdirs()) {
            throw new LifecycleException("Unable to create appBase " + appBase.getAbsolutePath() + " for Tomcat");
        }

        tomcat = new Tomcat();
        tomcat.getService().setName(configuration.getServerName());
        final String hostname = configuration.getBindAddress();
        tomcat.setHostname(hostname);
        tomcat.setPort(configuration.getBindHttpPort());
        tomcat.setBaseDir(tempDir.getAbsolutePath());
        tomcat.getConnector();

        // Enable JNDI - it is disabled by default.
        tomcat.enableNaming();

        tomcat.getEngine().setName(configuration.getServerName());

        // Instead of Tomcat.getHost() we create our own. Otherwise getHost() immediately adds the Host to
        // the Engine, which in turn calls Host.start(), and we don't want to start the host before we've had a
        // chance to add our own EmbeddedHostConfig as a LifecycleListener.
        host = new StandardHost();
        host.setName(hostname);
        host.setAppBase(appBase.getAbsolutePath());

        // We only want to deploy apps in accord with our DeployableContainer life cycle.
        host.setDeployOnStartup(false);
        host.setAutoDeploy(false);
        host.setConfigClass(EmbeddedContextConfig.class.getCanonicalName());

        embeddedHostConfig = new EmbeddedHostConfig();
        ((StandardHost) host).setUnpackWARs(configuration.isUnpackArchive());

        host.addLifecycleListener(embeddedHostConfig);

        tomcat.getEngine().addChild(host);

        tomcat.start();
        wasStarted = true;
    }

    protected void stopTomcatEmbedded() throws org.apache.catalina.LifecycleException {

        tomcat.stop();
        tomcat.destroy();
    }

    /**
     * Make sure the WAR file and unpacked directory (if applicable) are not left behind.
     *
     * @see {@link TomcatEmbeddedConfiguration#isUnpackArchive()}
     */
    private void deleteWar(final Archive<?> archive) {

        if (configuration.isUnpackArchive()) {
            final ContextName contextName = getContextName(archive);
            final File unpackDir = new File(host.getAppBase(), contextName.getBaseName());
            if (unpackDir.exists()) {
                ExpandWar.deleteDir(unpackDir);
            }
        }

        final File warFile = new File(host.getAppBase(), archive.getName());
        if (warFile.exists()) {
            warFile.delete();
        }
    }

    /**
     * Get the abstract pathname for the Tomcat home directory. The path will either be as specified by
     * {@link TomcatEmbeddedConfiguration#getTomcatHome()}, or if <code>null</code> a temporary path will be returned.
     * Either
     * underlying directory will be created with parents if necessary, and if created it will also be set to
     * {@link File#deleteOnExit()}.
     *
     * @return the Tomcat home directory path.
     *
     * @throws LifecycleException
     *     if the underlying directory could not be created.
     */
    private File getTomcatHomeFile() throws LifecycleException {

        // TODO this needs to be a lot more robust
        final String tomcatHome = configuration.getTomcatHome();
        File tomcatHomeFile;

        if (tomcatHome != null) {
            tomcatHomeFile = new File(systemPropertiesUtil.substituteEvironmentVariable(tomcatHome));

            if (!tomcatHomeFile.exists() && !tomcatHomeFile.mkdirs()) {
                throw new LifecycleException("Unable to create home directory for Tomcat");
            }

            tomcatHomeFile.deleteOnExit();
            return tomcatHomeFile;
        } else {
            try {
                tomcatHomeFile = File.createTempFile("tomcat-embedded-8", null);
                if (!tomcatHomeFile.delete() || !tomcatHomeFile.mkdirs()) {
                    throw new LifecycleException("Unable to create temporary home directory "
                        + tomcatHomeFile.getAbsolutePath() + " for Tomcat");
                }
                tomcatHomeFile.deleteOnExit();
                return tomcatHomeFile;
            } catch (final IOException e) {
                throw new LifecycleException("Unable to create temporary home directory for Tomcat", e);
            }
        }
    }

    /**
     * Get the Tomcat <code>ContextName</code> helper for the given Arquillian <code>Archive</code>.
     *
     * @param archive
     *     the Arquillian archive.
     *
     * @return the Tomcat context name helper.
     */
    private ContextName getContextName(final Archive<?> archive) {

        return new ContextName(archive.getName(), true);
    }
}
