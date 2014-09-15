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
package org.jboss.arquillian.container.tomcat.remote;

import java.io.IOException;
import java.net.URL;

import org.jboss.arquillian.container.spi.client.container.DeployableContainer;
import org.jboss.arquillian.container.spi.client.container.DeploymentException;
import org.jboss.arquillian.container.spi.client.container.LifecycleException;
import org.jboss.arquillian.container.spi.client.deployment.Validate;
import org.jboss.arquillian.container.spi.client.protocol.ProtocolDescription;
import org.jboss.arquillian.container.spi.client.protocol.metadata.ProtocolMetaData;
import org.jboss.arquillian.container.tomcat.ProtocolMetadataParser;
import org.jboss.arquillian.container.tomcat.ShrinkWrapUtil;
import org.jboss.arquillian.container.tomcat.TomcatManager;
import org.jboss.arquillian.container.tomcat.TomcatManagerCommandSpec;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.descriptor.api.Descriptor;

/**
 * <p>
 * Arquillian {@link DeployableContainer} implementation for an Remote Tomcat server; responsible for both deployment
 * operations.
 * </p>
 *
 * @author <a href="mailto:kpiwko@redhat.com">Karel Piwko</a>
 * @author <a href="mailto:ozizka@redhat.com">Ondrej Zizka</a>
 *
 * @version $Revision: $
 */
abstract class TomcatRemoteContainer implements DeployableContainer<TomcatRemoteConfiguration>
{
    private final ProtocolDescription protocolDescription;

    private final TomcatManagerCommandSpec tomcatManagerCommandSpec;

    /**
     * Tomcat container configuration
     */
    private TomcatRemoteConfiguration configuration;

    private TomcatManager<TomcatRemoteConfiguration> manager;

    TomcatRemoteContainer(final ProtocolDescription protocolDescription,
        final TomcatManagerCommandSpec tomcatManagerCommandSpec)
    {
        this.protocolDescription = protocolDescription;
        this.tomcatManagerCommandSpec = tomcatManagerCommandSpec;
    }

    @Override
    public Class<TomcatRemoteConfiguration> getConfigurationClass()
    {
        return TomcatRemoteConfiguration.class;
    }

    @Override
    public ProtocolDescription getDefaultProtocol()
    {
        return protocolDescription;
    }

    @Override
    public void setup(final TomcatRemoteConfiguration configuration)
    {
        this.configuration = configuration;
        this.manager = new TomcatManager<TomcatRemoteConfiguration>(configuration, tomcatManagerCommandSpec);
    }

    @Override
    public void start() throws LifecycleException
    {
        // no-op
    }

    @Override
    public void stop() throws LifecycleException
    {
        // no-op
    }

    @Override
    public void deploy(final Descriptor descriptor) throws DeploymentException
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void undeploy(final Descriptor descriptor) throws DeploymentException
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * Deploys to remote Tomcat using it's /manager web-app's org.apache.catalina.manager.ManagerServlet.
     *
     * @param archive
     * @return
     * @throws DeploymentException
     */
    @Override
    public ProtocolMetaData deploy(final Archive<?> archive) throws DeploymentException
    {
        Validate.notNull(archive, "Archive must not be null");

        final String archiveName = manager.normalizeArchiveName(archive.getName());
        final URL archiveURL = ShrinkWrapUtil.toURL(archive);
        try
        {
            manager.deploy("/" + archiveName, archiveURL);
        } catch (final IOException e)
        {
            throw new DeploymentException("Unable to deploy an archive " + archive.getName(), e);
        }

        final ProtocolMetadataParser<TomcatRemoteConfiguration> parser = new ProtocolMetadataParser<TomcatRemoteConfiguration>(
            configuration);
        return parser.retrieveContextServletInfo(archiveName);
    }

    @Override
    public void undeploy(final Archive<?> archive) throws DeploymentException
    {
        Validate.notNull(archive, "Archive must not be null");

        final String archiveName = manager.normalizeArchiveName(archive.getName());
        try
        {
            manager.undeploy("/" + archiveName);
        } catch (final IOException e)
        {
            throw new DeploymentException("Unable to undeploy an archive " + archive.getName(), e);
        }
    }
}
