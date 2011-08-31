/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.arquillian.container.tomcat.managed_6;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.codec.binary.Base64;
import org.jboss.arquillian.container.spi.client.container.DeploymentException;
import org.jboss.arquillian.container.tomcat.managed_6.util.IOUtils;
import org.jboss.arquillian.core.spi.Validate;

/**
 * Based on AbstractCatalinaTask, abstract base class for Ant tasks that interact with the <em>Manager</em> web application for
 * dynamically deploying and undeploying applications.
 * 
 * @author <a href="mailto:kpiwko@redhat.com">Karel Piwko</a>
 * @author Craig R. McClanahan
 * 
 */
public class TomcatManager {
    private static Logger log = Logger.getLogger(TomcatManager.class.getName());

    // encoding of manager web app
    private static final String MANAGER_CHARSET = "utf-8";

    private TomcatManagedConfiguration configuration;

    private String managerUrl;

    /**
     * Creates a Tomcat manager abstraction
     * 
     * @param configuration the configuration
     */
    public TomcatManager(TomcatManagedConfiguration configuration) {
        this.configuration = configuration;
        this.managerUrl = new StringBuilder("http://").append(configuration.getBindAddress()).append(":")
                .append(configuration.getBindHttpPort()).append("/manager").toString();
    }

    public void deploy(String name, URL content) throws IOException, DeploymentException {
        final String contentType = "application/octet-stream";
        Validate.notNullOrEmpty(name, "Name must not be null or empty");
        Validate.notNull(content, "Content to be deployed must not be null");

        URLConnection conn = content.openConnection();
        int contentLength = conn.getContentLength();
        InputStream stream = new BufferedInputStream(conn.getInputStream());

        // Building URL
        StringBuilder command = new StringBuilder("/deploy?path=");
        try {
            command.append(URLEncoder.encode(name, configuration.getUrlCharset()));
        } catch (UnsupportedEncodingException e) {
            throw new DeploymentException("Unable to construct path for Tomcat manager", e);
        }

        execute(command.toString(), stream, contentType, contentLength);
    }

    public void undeploy(String name) throws IOException, DeploymentException {
        Validate.notNullOrEmpty(name, "Undeployed name must not be null or empty");

        // Building URL
        StringBuilder command = new StringBuilder("/undeploy?path=");
        try {
            command.append(URLEncoder.encode(name, configuration.getUrlCharset()));
        } catch (UnsupportedEncodingException e) {
            throw new DeploymentException("Unable to construct path for Tomcat manager", e);
        }

        execute(command.toString(), null, null, -1);
    }

    public void list() throws IOException {
        execute("/list", null, null, -1);
    }

    public boolean isRunning() {
        try {
            execute("/list", null, null, -1);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public String normalizeArchiveName(String name) {
        Validate.notNull(name, "Archive name must not be empty");

        if (name.indexOf('.') != -1) {
            return name.substring(0, name.lastIndexOf("."));
        }

        return name;
    }

    /**
     * Execute the specified command, based on the configured properties. The input stream will be closed upon completion of
     * this task, whether it was executed successfully or not.
     * 
     * @param command Command to be executed
     * @param istream InputStream to include in an HTTP PUT, if any
     * @param contentType Content type to specify for the input, if any
     * @param contentLength Content length to specify for the input, if any
     * @throws IOException
     * @throws MalformedURLException
     * @throws DeploymentException
     * 
     * @exception BuildException if an error occurs
     */
    private void execute(String command, InputStream istream, String contentType, int contentLength) throws IOException {

        URLConnection conn = null;
        InputStreamReader reader = null;
        try {
            // Create a connection for this command
            conn = (new URL(managerUrl + command)).openConnection();
            HttpURLConnection hconn = (HttpURLConnection) conn;

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
            hconn.setRequestProperty("Authorization", constructHttpBasicAuthHeader());
            hconn.setRequestProperty("Accept", "text/plain");

            // Establish the connection with the server
            hconn.connect();

            // Send the request data (if any)
            if (istream != null) {
                BufferedOutputStream ostream = new BufferedOutputStream(hconn.getOutputStream(), 1024);
                IOUtils.copy(istream, ostream);
                ostream.flush();
                ostream.close();
                istream.close();
            }
            
            int responseCode = hconn.getResponseCode();
            System.out.println("responseCode = " + responseCode);

            // Process the response message
            reader = new InputStreamReader(hconn.getInputStream(), MANAGER_CHARSET);
            StringBuilder sb = new StringBuilder();
            String error = null;
            boolean first = true;
            while (true) {
                int ch = reader.read();
                if (ch < 0) {
                    break;
                } else if ((ch == '\r') || (ch == '\n')) {
                    // in Win \r\n would cause handleOutput() to be called
                    // twice, the second time with an empty string,
                    // producing blank lines
                    if (sb.length() > 0) {
                        String line = sb.toString();
                        sb.setLength(0);
                        if (first) {
                            if (!line.startsWith("OK -")) {
                                error = line;
                            }
                            first = false;
                        }
                        if (log.isLoggable(Level.FINE)) {
                            log.fine(line);
                        }
                    }
                } else {
                    sb.append((char) ch);
                }
            }
            if (sb.length() > 0) {
                if (log.isLoggable(Level.FINE)) {
                    log.fine(sb.toString());
                }
            }
            if (error != null) {
                throw new RuntimeException("Unable to executed command " + command + " on server, received " + error);
            }
        } finally {
            IOUtils.closeQuietly(reader);
            IOUtils.closeQuietly(istream);
        }
    }

    private String constructHttpBasicAuthHeader() {
        // Set up an authorization header with our credentials
        String credentials = configuration.getUser() + ":" + configuration.getPass();
        return "Basic "
                + new String(Base64.encodeBase64(credentials.getBytes(Charset.defaultCharset())), Charset.defaultCharset());
    }

}
