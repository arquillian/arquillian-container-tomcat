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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;

import org.jboss.arquillian.container.spi.client.container.DeployableContainer;
import org.jboss.arquillian.container.spi.client.container.DeploymentException;
import org.jboss.arquillian.container.spi.client.container.LifecycleException;
import org.jboss.arquillian.container.spi.client.protocol.ProtocolDescription;
import org.jboss.arquillian.container.spi.client.protocol.metadata.ProtocolMetaData;
import org.jboss.arquillian.container.tomcat.ProtocolMetadataParser;
import org.jboss.arquillian.container.tomcat.ShrinkWrapUtil;
import org.jboss.arquillian.container.tomcat.StringUtils;
import org.jboss.arquillian.container.tomcat.SystemUtils;
import org.jboss.arquillian.container.tomcat.TomcatManager;
import org.jboss.arquillian.container.tomcat.TomcatManagerCommandSpec;
import org.jboss.arquillian.container.tomcat.Validate;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.descriptor.api.Descriptor;

/**
 * <p>
 * Arquillian {@link org.jboss.arquillian.container.spi.client.container.DeployableContainer} implementation for an Managed
 * Tomcat server; responsible for both lifecycle and deployment operations.
 * </p>
 *
 * @author <a href="mailto:kpiwko@redhat.com">Karel Piwko</a>
 * @author <a href="mailto:jhuska@redhat.com">Juraj Huska</a>
 * @author <a href="mailto:ozizka@redhat.com">Ondrej Zizka</a>
 * @author <a href="mailto:steve.coy@me.com">Stephen Coy</a>
 * @version $Revision: $
 */
abstract class TomcatManagedContainer implements DeployableContainer<TomcatManagedConfiguration> {

    private static final Logger log = Logger.getLogger(TomcatManagedContainer.class.getName());

    private final EnvironmentStrategy environmentStrategy = new EnvironmentStrategy();

    private final TomcatManagerCommandSpec tomcatManagerCommandSpec;

    private final ProtocolDescription protocolDescription;

    private TomcatManagedConfiguration configuration;

    private TomcatManager<? extends TomcatManagedConfiguration> manager;

    private Thread shutdownThread;

    private Process startupProcess;

    TomcatManagedContainer(final ProtocolDescription protocolDescription,
        final TomcatManagerCommandSpec tomcatManagerCommandSpec) {

        this.protocolDescription = protocolDescription;
        this.tomcatManagerCommandSpec = tomcatManagerCommandSpec;
    }

    @Override
    public Class<TomcatManagedConfiguration> getConfigurationClass() {

        return TomcatManagedConfiguration.class;
    }

    @Override
    public ProtocolDescription getDefaultProtocol() {

        return protocolDescription;
    }

    @Override
    public void setup(final TomcatManagedConfiguration configuration) {

        this.configuration = configuration;
        this.manager = new TomcatManager<TomcatManagedConfiguration>(configuration, tomcatManagerCommandSpec);
    }

    @Override
    public void start() throws LifecycleException {

        // FIXME: ARQ-1619 - Tomcat managed container ignores allowConnectingToRunningServer
        if (manager.isRunning()) {
            throw new LifecycleException("The server is already running! "
                + "Managed containers does not support connecting to running server instances due to the "
                + "possible harmful effect of connecting to the wrong server. Please stop server before running or "
                + "change to another type of container.\n"
                + "To disable this check and allow Arquillian to connect to a running server, "
                + "set allowConnectingToRunningServer to true in the container configuration");
        }

        try {

            final List<String> startupCommand = new ArrayList<String>();

            final String binDirPath = configuration.getCatalinaHome() + File.separator + "bin";

            final String startupScript =
                binDirPath + File.separator + "catalina" + (SystemUtils.IS_OS_WINDOWS ? ".bat" : ".sh");

            startupCommand.add(startupScript);

            // TODO: Add to configuration so "jpda" can be used as well...
            startupCommand.add("run");

            if (!StringUtils.isBlank(configuration.getServerConfig())) {

                startupCommand.add("-config");
                startupCommand.add("conf/" + File.separator + configuration.getServerConfig());
            }

            final ProcessBuilder startupProcessBuilder = new ProcessBuilder(startupCommand);

            startupProcessBuilder.directory(new File(binDirPath));

            final Map<String, String> environmentMap = startupProcessBuilder.environment();

            environmentStrategy.applyEnvironment(configuration, environmentMap);

            startupProcessBuilder.redirectErrorStream(true);

            log.info("Starting Tomcat with command: " + startupCommand + " and environment: " + environmentMap);

            startupProcess = startupProcessBuilder.start();

            new Thread(new ConsoleConsumer(configuration.isOutputToConsole())).start();

            final Process proc = startupProcess;

            shutdownThread = new Thread(new Runnable() {

                @Override
                public void run() {

                    if (proc != null) {
                        proc.destroy();
                        try {
                            proc.waitFor();
                        } catch (final InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            });
            Runtime.getRuntime().addShutdownHook(shutdownThread);

            final long startupTimeout = configuration.getStartupTimeoutInSeconds();
            long timeout = startupTimeout * 1000;
            boolean serverAvailable = false;
            while (timeout > 0 && serverAvailable == false) {
                serverAvailable = manager.isRunning();
                if (!serverAvailable) {
                    Thread.sleep(100);
                    timeout -= 100;
                }
            }
            if (!serverAvailable) {
                destroystartupProcess();
                throw new TimeoutException(String.format("Managed server was not started within [%d] s", startupTimeout));
            }

        } catch (final Exception ex) {

            throw new LifecycleException("Could not start container", ex);
        }

    }

    @Override
    public void stop() throws LifecycleException {

        // TODO: Switch to catalina.sh start/stop instead of run/[kill]?

        if (shutdownThread != null) {
            Runtime.getRuntime().removeShutdownHook(shutdownThread);
            shutdownThread = null;
        }
        try {
            if (startupProcess != null) {
                startupProcess.destroy();
                startupProcess.waitFor();
                startupProcess = null;
            }
        } catch (final Exception e) {
            throw new LifecycleException("Could not stop container", e);
        }
    }

    /**
     * Deploys to remote Tomcat using it's /manager web-app's org.apache.catalina.manager.ManagerServlet.
     *
     * @param archive
     * @return
     * @throws org.jboss.arquillian.container.spi.client.container.DeploymentException
     */
    @Override
    public ProtocolMetaData deploy(final Archive<?> archive) throws DeploymentException {

        Validate.notNull(archive, "Archive must not be null");

        final String archiveName = manager.normalizeArchiveName(archive.getName());
        final URL archiveURL = ShrinkWrapUtil.toURL(archive);
        try {
            manager.deploy("/" + archiveName, archiveURL);
        } catch (final IOException e) {
            throw new DeploymentException("Unable to deploy an archive " + archive.getName(), e);
        }

        final ProtocolMetadataParser<TomcatManagedConfiguration> parser =
            new ProtocolMetadataParser<TomcatManagedConfiguration>(configuration);
        return parser.retrieveContextServletInfo(archiveName);
    }

    @Override
    public void undeploy(final Archive<?> archive) throws DeploymentException {

        Validate.notNull(archive, "Archive must not be null");

        final String archiveName = manager.normalizeArchiveName(archive.getName());
        try {
            manager.undeploy("/" + archiveName);
        } catch (final IOException e) {
            throw new DeploymentException("Unable to undeploy an archive " + archive.getName(), e);
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

    /**
     * Runnable that consumes the output of the startupProcess. If nothing consumes the output the AS will hang on some
     * platforms
     *
     * @author Stuart Douglas
     */
    private class ConsoleConsumer implements Runnable {

        private final boolean writeOutput;

        ConsoleConsumer(final boolean writeOutput) {

            this.writeOutput = writeOutput;
        }

        @Override
        public void run() {

            final InputStream stream = startupProcess.getInputStream();
            final BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
            String line = null;
            try {
                while ((line = reader.readLine()) != null) {
                    if (writeOutput) {
                        System.out.println(line);
                    }
                }
            } catch (final IOException e) {
            }
        }

    }

    private int destroystartupProcess() {

        if (startupProcess == null)
            return 0;
        startupProcess.destroy();
        try {
            return startupProcess.waitFor();
        } catch (final InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
