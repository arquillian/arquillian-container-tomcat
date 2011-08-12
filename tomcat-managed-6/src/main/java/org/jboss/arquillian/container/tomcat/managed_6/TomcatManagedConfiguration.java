/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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

import java.net.URI;
import java.net.URISyntaxException;
import java.security.AccessController;
import java.security.PrivilegedAction;

import org.jboss.arquillian.container.spi.ConfigurationException;
import org.jboss.arquillian.container.spi.client.container.ContainerConfiguration;
import org.jboss.arquillian.container.tomcat.managed_6.util.Validate;

//import org.jboss.arquillian.container.spi.client.deployment.Validate;

/**
 * Arquillian Tomcat Container Configuration
 *
 * @author <a href="mailto:jhuska@redhat.com">Juraj Huska</a>
 * @version $Revision: $
 */
public class TomcatManagedConfiguration implements ContainerConfiguration {

    private static final int MAX_PORT = 65535;

    private String bindAddress = "localhost";

    private int bindHttpPort = 8080;

    private String user;

    private String pass;

    private int jmxPort = 8089;

    private URI jmxUrl;

    private String urlCharset = "ISO-8859-1";

    private boolean writeOutputToConsole = true;

    private String catalinaHome = System.getenv("CATALINA_HOME");

    private String javaHome = System.getenv("JAVA_HOME");

    private String javaVmArguments = "-Xmx512m -XX:MaxPermSize=128m";

    private int startupTimeoutInSeconds = 120;

    private int shutdownTimeoutInSeconds = 45;

    private String appBase = "webapps";

    private boolean unpackArchive = false;

    private String workDir = null;

    private String serverName = "arquillian-tomcat-managed-6";

    private String serverConfig = "server.xml";

    @Override
    public void validate() throws ConfigurationException {

        if (this.jmxPort > MAX_PORT)
            throw new ConfigurationException("JMX port larger than " + MAX_PORT + ": " + this.jmxPort);

        try {
            this.jmxUrl = new URI("service:jmx:rmi:///jndi/rmi://" + this.bindAddress + ":" + this.jmxPort + "/jmxrmi");
        } catch (URISyntaxException ex) {
            throw new ConfigurationException(ex.getMessage(), ex);
        }

        Validate.configurationDirectoryExists(
                catalinaHome,
                "Either CATALINA_HOME environment variable or catalinaHome property in Arquillian configuration must be set and point to a valid directory! "
                        + catalinaHome + " is not valid directory!");
        Validate.configurationDirectoryExists(
                javaHome,
                "Either JAVA_HOME environment variable or javaHome property in Arquillian configuration must be set and point to a valid directory! "
                        + javaHome + " is not valid directory!");

        Validate.isValidFile(catalinaHome + "/conf/" + serverConfig,
                "The server configuration file denoted by serverConfig property has to exist! This file: " + catalinaHome
                        + "/conf/" + serverConfig + " does not!");

        // set write output to console
        this.writeOutputToConsole = AccessController.doPrivileged(new PrivilegedAction<Boolean>() {
            @Override
            public Boolean run() {
                // By default, redirect to stdout unless disabled by this property
                String val = System.getProperty("org.apache.tomcat.writeconsole");
                return val == null || !"false".equals(val);
            }
        });
    }

    public String getBindAddress() {
        return bindAddress;
    }

    public void setBindAddress(String bindAddress) {
        this.bindAddress = bindAddress;
    }

    public int getBindHttpPort() {
        return bindHttpPort;
    }

    /**
     * Set the HTTP bind port.
     *
     * @param httpBindPort HTTP bind port
     */
    public void setBindHttpPort(int bindHttpPort) {
        this.bindHttpPort = bindHttpPort;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPass() {
        return pass;
    }

    public void setPass(String pass) {
        this.pass = pass;
    }

    public int getJmxPort() {
        return jmxPort;
    }

    public void setJmxPort(int jmxPort) {
        this.jmxPort = jmxPort;
    }

    public URI getJmxUrl() {
        return jmxUrl;
    }

    public void setJmxUrl(URI jmxUrl) {
        this.jmxUrl = jmxUrl;
    }

    public String getCatalinaHome() {
        return catalinaHome;
    }

    public void setCatalinaHome(String catalinaHome) {
        this.catalinaHome = catalinaHome;
    }

    public String getJavaHome() {
        return javaHome;
    }

    public void setJavaHome(String javaHome) {
        this.javaHome = javaHome;
    }

    public String getJavaVmArguments() {
        return javaVmArguments;
    }

    /**
     * This will override the default ("-Xmx512m -XX:MaxPermSize=128m") startup JVM arguments.
     *
     * @param javaVmArguments use as start up arguments
     */
    public void setJavaVmArguments(String javaVmArguments) {
        this.javaVmArguments = javaVmArguments;
    }

    public int getStartupTimeoutInSeconds() {
        return startupTimeoutInSeconds;
    }

    public void setStartupTimeoutInSeconds(int startupTimeoutInSeconds) {
        this.startupTimeoutInSeconds = startupTimeoutInSeconds;
    }

    public int getShutdownTimeoutInSeconds() {
        return shutdownTimeoutInSeconds;
    }

    public void setShutdownTimeoutInSeconds(int shutdownTimeoutInSeconds) {
        this.shutdownTimeoutInSeconds = shutdownTimeoutInSeconds;
    }

    public String getAppBase() {
        return appBase;
    }

    /**
     * @param appBase the directory where the deployed webapps are stored within the Tomcat installation
     */
    public void setAppBase(String appBase) {
        this.appBase = appBase;
    }

    /**
     * @return a switch indicating whether the WAR should be unpacked
     */
    public boolean isUnpackArchive() {
        return unpackArchive;
    }

    /**
     * Sets the WAR to be unpacked into the java.io.tmpdir when deployed. Unpacking is required if you are using Weld to provide
     * CDI support in a servlet environment.
     *
     * @param a switch indicating whether the WAR should be unpacked
     */
    public void setUnpackArchive(boolean unpackArchive) {
        this.unpackArchive = unpackArchive;
    }

    public String getWorkDir() {
        return workDir;
    }

    /**
     * @param workDir the directory where the compiled JSP files and session serialization data is stored
     */
    public void setWorkDir(String workDir) {
        this.workDir = workDir;
    }

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public String getServerConfig() {
        return serverConfig;
    }

    public void setServerConfig(String serverConfig) {
        this.serverConfig = serverConfig;
    }

    /**
     * @param urlCharset the urlCharset to set
     */
    public void setUrlCharset(String urlCharset) {
        this.urlCharset = urlCharset;
    }

    /**
     * @return the urlCharset
     */
    public String getUrlCharset() {
        return urlCharset;
    }

    /**
     * @param writeOutputToConsole the writeOutputToConsole to set
     */
    public void setWriteOutputToConsole(boolean writeOutputToConsole) {
        this.writeOutputToConsole = writeOutputToConsole;
    }

    /**
     * @return the writeOutputToConsole
     */
    public boolean isWriteOutputToConsole() {
        return writeOutputToConsole;
    }

}
