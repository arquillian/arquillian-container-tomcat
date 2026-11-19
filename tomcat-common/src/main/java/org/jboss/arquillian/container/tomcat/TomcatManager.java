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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jboss.arquillian.container.spi.ConfigurationException;
import org.jboss.arquillian.container.spi.client.container.DeploymentException;

/**
 * Based on AbstractCatalinaTask, abstract base class for Ant tasks that interact with the <em>Manager</em> web
 * application for
 * dynamically deploying and undeploying applications.
 *
 * @author <a href="mailto:kpiwko@redhat.com">Karel Piwko</a>
 * @author Craig R. McClanahan
 * @author Radoslav Husar
 */
public class TomcatManager<C extends TomcatConfiguration> {

    private static Logger log = Logger.getLogger(TomcatManager.class.getName());

    // encoding of manager web app
    protected static final String MANAGER_CHARSET = "utf-8";

    private final C configuration;

    private final TomcatManagerCommandSpec tomcatManagerCommandSpec;

    /**
     * Creates a Tomcat manager abstraction
     *
     * @param configuration
     *     the configuration
     */
    public TomcatManager(final C configuration, final TomcatManagerCommandSpec tomcatManagerCommandSpec) {

        this.configuration = configuration;
        this.tomcatManagerCommandSpec = tomcatManagerCommandSpec;
    }

    public void deploy(final ContextName contextName, final URL content) throws IOException, DeploymentException {

        final String contentType = "application/octet-stream";
        Validate.notNull(contextName, "Context name must not be null");
        Validate.notNull(content, "Content to be deployed must not be null");

        final URLConnection conn = content.openConnection();
        final int contentLength = conn.getContentLength();
        final InputStream stream = new BufferedInputStream(conn.getInputStream());

        final String command = buildCommand(tomcatManagerCommandSpec.getDeployCommand(), contextName);

        execute(command, stream, contentType, contentLength);
    }

    public void undeploy(final ContextName contextName) throws IOException, DeploymentException {

        Validate.notNull(contextName, "Context name must not be null");

        final String command = buildCommand(tomcatManagerCommandSpec.getUndeployCommand(), contextName);

        execute(command, null, null, -1);
    }

    /**
     * Appends the "path" request parameter, and for a parallel deployment the "version" parameter, that the manager
     * uses to determine the context the archive is deployed to.
     */
    private String buildCommand(final String managerCommand, final ContextName contextName) throws DeploymentException {

        final StringBuilder command = new StringBuilder(managerCommand);

        // The root context has an empty path, which the manager addresses as "/".
        final String path = contextName.getPath().isEmpty() ? "/" : contextName.getPath();
        final String version = contextName.getVersion();

        try {
            command.append(URLEncoder.encode(path, configuration.getUrlCharset()));

            if (!version.isEmpty()) {
                command.append("&version=").append(URLEncoder.encode(version, configuration.getUrlCharset()));
            }
        } catch (final UnsupportedEncodingException e) {
            throw new DeploymentException("Unable to construct path for Tomcat manager", e);
        }

        return command.toString();
    }

    public void serverInfo() throws IOException {
        execute(tomcatManagerCommandSpec.getServerInfoCommand(), null, null, -1);
    }

    /**
     * This method is deprecated, please use {@link TomcatManager#serverInfo()} instead, to find out if container is
     * running.
     */
    @Deprecated
    public void list() throws IOException {

        execute(tomcatManagerCommandSpec.getListCommand(), null, null, -1);
    }

    public boolean isRunning() {

        try {
            serverInfo();
            return true;
        } catch (final IOException e) {
            return false;
        }
    }

    /**
     * Execute the specified command, based on the configured properties. The input stream will be closed upon completion
     * of
     * this task, whether it was executed successfully or not.
     *
     * @param command
     *     Command to be executed
     * @param istream
     *     InputStream to include in an HTTP PUT, if any
     * @param contentType
     *     Content type to specify for the input, if any
     * @param contentLength
     *     Content length to specify for the input, if any
     *
     * @throws IOException
     * @throws MalformedURLException
     * @throws DeploymentException
     */
    protected void execute(final String command, final InputStream istream, final String contentType,
        final int contentLength)
        throws IOException {

        URLConnection conn = null;
        try {
            // Create a connection for this command
            conn = new URL(configuration.getManagerUrl() + command).openConnection();
            final HttpURLConnection hconn = (HttpURLConnection) conn;

            // Set up standard connection characteristics
            hconn.setAllowUserInteraction(false);
            hconn.setDoInput(true);
            hconn.setUseCaches(false);
            if (istream != null) {
                hconn.setDoOutput(true);
                hconn.setRequestMethod("PUT");
                if (contentType != null) {
                    hconn.setRequestProperty("Content-Type", contentType);
                }
                if (contentLength >= 0) {
                    hconn.setRequestProperty("Content-Length", "" + contentLength);

                    hconn.setFixedLengthStreamingMode(contentLength);
                }
            } else {
                hconn.setDoOutput(false);
                hconn.setRequestMethod("GET");
            }
            hconn.setRequestProperty("User-Agent", "Arquillian-Tomcat-Manager-Util/1.0");
            // add authorization header if password is provided
            if (configuration.getUser() != null && configuration.getUser().length() != 0) {
                hconn.setRequestProperty("Authorization", constructHttpBasicAuthHeader());
            }
            hconn.setRequestProperty("Accept", "text/plain");

            // Establish the connection with the server
            hconn.connect();

            // Send the request data (if any)
            if (istream != null) {
                final BufferedOutputStream ostream = new BufferedOutputStream(hconn.getOutputStream(), 1024);
                IOUtil.copy(istream, ostream);
                ostream.flush();
                ostream.close();
                istream.close();
            }

            processResponse(command, hconn);
        } finally {
            IOUtil.closeQuietly(istream);
        }
    }

    protected void processResponse(final String command, final HttpURLConnection hconn) throws IOException {

        final int httpResponseCode = hconn.getResponseCode();
        // Supposes that <= 199 is not bad, but is it? See http://en.wikipedia.org/wiki/List_of_HTTP_status_codes
        if (httpResponseCode >= 400 && httpResponseCode < 500) {
            throw new ConfigurationException(
                "Unable to connect to Tomcat manager. "
                    + "The server command ("
                    + command
                    + ") failed with responseCode ("
                    + httpResponseCode
                    + ") and responseMessage ("
                    + hconn.getResponseMessage()
                    + ").\n\n"
                    + "Please make sure that you provided correct credentials to an user which is able to access Tomcat manager application.\n"
                    + "These credentials can be specified in the Arquillian container configuration as \"user\" and \"pass\" properties.\n"
                    + "The user must have aapropriate role specified in tomcat-users.xml file.\n");
        } else if (httpResponseCode >= 300) {
            throw new IllegalStateException("The server command (" + command + ") failed with responseCode ("
                + httpResponseCode + ") and responseMessage (" + hconn.getResponseMessage() + ").");
        }
        BufferedReader reader = null;
        try {
            // Process the response message
            reader = new BufferedReader(new InputStreamReader(hconn.getInputStream(), MANAGER_CHARSET));
            String line = reader.readLine();
            String contentError = null;
            if (line != null && !line.startsWith("OK -")) {
                contentError = line;
            }
            while (line != null) {
                if (log.isLoggable(Level.FINE)) {
                    log.fine(line);
                }
                line = reader.readLine();
            }
            if (contentError != null) {
                throw new RuntimeException(
                    "The server command (" + command + ") failed with content (" + contentError + ").");
            }
        } finally {
            IOUtil.closeQuietly(reader);
        }
    }

    protected String constructHttpBasicAuthHeader() {
        // Set up an authorization header with our credentials
        final String credentials = configuration.getUser() + ":" + configuration.getPass();

        // Encodes the user:password pair as a sequence of ISO-8859-1 bytes.
        // We'll return the Base64 encoded form of this ISO-8859-1 byte sequence.
        byte[] credentialsBytes = credentials.getBytes(StandardCharsets.ISO_8859_1);

        return "Basic " + Base64.getEncoder().encodeToString(credentialsBytes);
    }

}
