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

import java.security.AccessController;
import java.security.PrivilegedAction;

import org.jboss.arquillian.container.spi.ConfigurationException;
import org.jboss.arquillian.container.tomcat.StringUtils;
import org.jboss.arquillian.container.tomcat.TomcatConfiguration;

/**
 * Arquillian Tomcat Managed container configuration.
 *
 * These properties mirror the environment variables used by the Tomcat distribution's platform-specific "catalina" script, as
 * called by the "startup" and "shutdown" scripts. These properties are used to set the environment for the invocation of those
 * scripts.
 *
 * Unless otherwise noted most of these properties will take their default values from the environment Arquillian is run from by
 * way of {@link System#getenv(String)}. If a given environment variable is not set, or is set to the empty string, it will not
 * be used. Any properties explicitly set on this configuration (e.g. via arquillian.xml) will override the environment
 * defaults.
 *
 * Any properties set in Tomcat's "setenv" script, if present, will be used by the startup and shutdown scripts instead of the
 * values from this configuration.
 *
 * See the Tomcat distribution's RUNNING.txt documentation, and the "catalina" script itself for further details.
 *
 * @author <a href="mailto:kpiwko@redhat.com">Karel Piwko</a>
 * @author <a href="mailto:jhuska@redhat.com">Juraj Huska</a>
 * @author <a href="mailto:steve.coy@me.com">Stephen Coy</a>
 * @author <a href="mailto:ian@ianbrandt.com">Ian Brandt</a>
 * @see <a href="http://tomcat.apache.org/tomcat-6.0-doc/RUNNING.txt">RUNNING.txt for Tomcat 6</a>
 * @see <a href="http://tomcat.apache.org/tomcat-7.0-doc/RUNNING.txt">RUNNING.txt for Tomcat 7</a>
 * @see <a href="http://tomcat.apache.org/tomcat-8.0-doc/RUNNING.txt">RUNNING.txt for Tomcat 8</a>
 *
 * @version $Revision: $
 */
public class TomcatManagedConfiguration extends TomcatConfiguration {

    static final String CATALINA_HOME = "CATALINA_HOME";

    static final String CATALINA_BASE = "CATALINA_BASE";

    static final String CATALINA_OUT = "CATALINA_OUT";

    static final String CATALINA_OPTS = "CATALINA_OPTS";

    static final String CATALINA_PID = "CATALINA_PID";

    static final String CATALINA_TMPDIR = "CATALINA_TMPDIR";

    static final String JAVA_HOME = "JAVA_HOME";

    static final String JAVA_HOME_SYSTEM_PROPERTY = "java.home";

    static final String JRE_HOME = "JRE_HOME";

    static final String JAVA_OPTS = "JAVA_OPTS";

    static final String JAVA_ENDORSED_DIRS = "JAVA_ENDORSED_DIRS";

    static final String JPDA_TRANSPORT = "JPDA_TRANSPORT";

    static final String JPDA_ADDRESS = "JPDA_ADDRESS";

    static final String JPDA_SUSPEND = "JPDA_SUSPEND";

    static final String JPDA_OPTS = "JPDA_OPTS";

    static final String LOGGING_CONFIG = "LOGGING_CONFIG";

    static final String LOGGING_MANAGER = "LOGGING_MANAGER";

    private String catalinaHome = System.getenv(CATALINA_HOME);

    private String catalinaBase = System.getenv(CATALINA_BASE);

    private String catalinaOut = System.getenv(CATALINA_OUT);

    private String catalinaOpts = System.getenv(CATALINA_OPTS);

    private String catalinaPid = System.getenv(CATALINA_PID);

    private String catalinaTmpDir = System.getenv(CATALINA_TMPDIR);

    private String javaHome =
        StringUtils.isBlank(System.getenv(JAVA_HOME)) ? System.getProperty(JAVA_HOME_SYSTEM_PROPERTY) : System
            .getenv(JAVA_HOME); // ARQ-744 - Default to the current VM if the JAVA_HOME environment variable is not set.

    private String jreHome = System.getenv(JRE_HOME);

    private String javaOpts = System.getenv(JAVA_OPTS);

    private String javaEndorsedDirs = System.getenv(JAVA_ENDORSED_DIRS);

    private String jpdaTransport = System.getenv(JPDA_TRANSPORT);

    private String jpdaAddress = System.getenv(JPDA_ADDRESS);

    private String jpdaSuspend = System.getenv(JPDA_SUSPEND);

    private String jpdaOpts = System.getenv(JPDA_OPTS);

    private String loggingConfig = System.getenv(LOGGING_CONFIG);

    private String loggingManager = System.getenv(LOGGING_MANAGER);

    private int startupTimeoutInSeconds = 120;

    private int shutdownTimeoutInSeconds = 45;

    private boolean outputToConsole = true;

    private String serverConfig = null;

    private String loggingProperties;

    @Override
    public void validate() throws ConfigurationException {

        super.validate();

        // set write output to console
        this.setOutputToConsole(AccessController.doPrivileged(new PrivilegedAction<Boolean>() {

            public Boolean run() {

                // By default, redirect to stdout unless disabled by this property
                final String val = System.getProperty("org.apache.tomcat.writeconsole");
                return val == null || !"false".equals(val);
            }
        }));
    }

    /**
     * @return the absolute path to the "binary" distribution of Tomcat
     */
    public String getCatalinaHome() {

        return catalinaHome;
    }

    /**
     * (Required) The absolute path to the "binary" distribution of Tomcat
     *
     * @param catalinaHome must be a valid absolute path to an existing directory
     */
    public void setCatalinaHome(final String catalinaHome) {

        this.catalinaHome = catalinaHome;
    }

    /**
     * @return the absolute path to the "active configuration" of Tomcat
     */
    public String getCatalinaBase() {

        return catalinaBase;
    }

    /**
     * (Optional) The absolute path to the "active configuration" of Tomcat
     *
     * @param catalinaBase if set must be a valid absolute path to an existing directory
     */
    public void setCatalinaBase(final String catalinaBase) {

        this.catalinaBase = catalinaBase;
    }

    /**
     * @return the full path to a file where stdout and stderr will be redirected
     * @see {@link #setCatalinaOut(String)} for further details
     */
    public String getCatalinaOut() {

        return catalinaOut;
    }

    /**
     * (Optional) Full path to a file where stdout and stderr will be redirected. Default is $CATALINA_BASE/logs/catalina.out
     *
     * @param catalinaOut if set must be a valid absolute path to an existing writable file
     */
    public void setCatalinaOut(final String catalinaOut) {

        this.catalinaOut = catalinaOut;
    }

    /**
     * @return the options for the java command that starts Tomcat
     * @see {@link #setCatalinaOpts(String)} for further details
     */
    public String getCatalinaOpts() {

        return catalinaOpts;
    }

    /**
     * (Optional) The options for the java command that starts Tomcat.
     *
     * See the Java documentation for the options that affect the Java Runtime EnvironmentFacade.
     *
     * See the "System Properties" page in the Tomcat Configuration Reference for the system properties that are specific to
     * Tomcat.
     *
     * @param catalinaOpts the options for the java command that starts Tomcat
     */
    public void setCatalinaOpts(final String catalinaOpts) {

        this.catalinaOpts = catalinaOpts;
    }

    /**
     * @return the absolute path of the file which should contain the process ID of the Catalina java process
     * @see {@link #setCatalinaPid(String)} for further details
     */
    public String getCatalinaPid() {

        return catalinaPid;
    }

    /**
     * (Optional) The absolute path of the file which should contain the process ID of the Catalina java process
     *
     * @param catalinaPid if set must be the absolute path to a writable file
     */
    public void setCatalinaPid(final String catalinaPid) {

        this.catalinaPid = catalinaPid;
    }

    /**
     * @return the absolute path of the temporary directory the JVM should use
     * @see {@link #setCatalinaTmpDir(String)} for further details
     */
    public String getCatalinaTmpDir() {

        return catalinaTmpDir;
    }

    /**
     * (Optional) The absolute path of the temporary directory the JVM should use (java.io.tmpdir).
     *
     * @param catalinaTmpDir if set must be a valid absolute path to an existing JDK directory
     */
    public void setCatalinaTmpDir(final String catalinaTmpDir) {

        this.catalinaTmpDir = catalinaTmpDir;
    }

    /**
     * @return the absolute path of the JDK used to start Tomcat
     * @see {@link #setJavaHome(String)} for further details
     */
    public String getJavaHome() {

        return javaHome;
    }

    /**
     * The absolute path to the JDK to use to start Tomcat. Defaults to the value of the "JAVA_HOME" environment variable if
     * set. Otherwise it defaults to the current VM by way of {@link System#getProperty(String)} for "java.home".
     *
     * Either a JDK or JRE must be specified, but this can be done by way of a "setenv" script instead of this configuration.
     * See the Tomcat distribution's RUNNING.txt documentation for further details.
     *
     * If both {@link #setJreHome(String)} and {@link #setJavaHome(String)} are specified, the <code>jreHome</code> is used.
     *
     * Using <code>javaHome</code> provides access to certain additional startup options that are not allowed when
     * <code>jreHome</code> is used.
     *
     * @param javaHome if set must be a valid absolute path to an existing JDK directory
     */
    public void setJavaHome(final String javaHome) {

        this.javaHome = javaHome;
    }

    /**
     * @return the absolute path of the JRE used to start Tomcat
     * @see {@link #setJreHome(String)} and in particular {@link #setJavaHome(String)} for further details
     */
    public String getJreHome() {

        return jreHome;
    }

    /**
     * (Optional) The absolute path of a JRE to use to start Tomcat.
     *
     * @param jreHome if set must be a valid absolute path to an existing JRE directory
     * @see {@link #setJavaHome(String)} for further details
     */
    public void setJreHome(final String jreHome) {

        this.jreHome = jreHome;
    }

    /**
     * @return less common Java options used for both starting and stopping Tomcat, as well as for other commands
     * @see {@link #setJavaOpts(String)} for further details
     */
    public String getJavaOpts() {

        return javaOpts;
    }

    /**
     * (Optional) Less common Java options used for both starting and stopping Tomcat, as well as for other commands.
     *
     * Note: Do not use <code>javaOpts</code> to specify memory limits. You do not need much memory for the small process that
     * is used to stop Tomcat. Use {@link #setCatalinaOpts(String)} instead for options that should apply only to the primary
     * Tomcat process.
     *
     * @param javaOpts the Java options used for both starting and stopping Tomcat, as well as for other commands
     */
    public void setJavaOpts(final String javaOpts) {

        this.javaOpts = javaOpts;
    }

    /**
     * @return the list of colon (Unix) or semi-colon (Windows) separated directories containing endorsed jars
     * @see {@link #setJavaEndorsedDirs(String)} for further details
     */
    public String getJavaEndorsedDirs() {

        return javaEndorsedDirs;
    }

    /**
     * (Optional) List of colon (Unix) or semi-colon (Windows) separated directories containing jars to replace of APIs created
     * outside of the JCP (i.e. DOM and SAX from W3C). It can also be used to update the XML parser implementation.
     *
     * @param javaEndorsedDirs if set must be a valid platform-specific path list of existing directories
     */
    // TODO: Support a platform agnostic path list by swapping colons and semi-colons as appropriate for the current OS.
    public void setJavaEndorsedDirs(final String javaEndorsedDirs) {

        this.javaEndorsedDirs = javaEndorsedDirs;
    }

    /**
     * @return the JPDA transport used when the "jpda start" command is executed
     * @see {@link #setJpdaTransport(String)} for further details
     */
    public String getJpdaTransport() {

        return jpdaTransport;
    }

    /**
     * (Optional) The JPDA transport used when the "jpda start" command is executed. The default is "dt_socket".
     *
     * @param jpdaTransport if set must be a valid JPDA transport
     */
    public void setJpdaTransport(final String jpdaTransport) {

        this.jpdaTransport = jpdaTransport;
    }

    /**
     * @return the JPDA address used when the "jpda start" command is executed.
     * @see {@link #setJpdaAddress(String)} for further details
     */
    public String getJpdaAddress() {

        return jpdaAddress;
    }

    /**
     * (Optional) The JPDA address used when the "jpda start" command is executed. The default is 8000.
     *
     * @param jpdaAddress if set must be a valid JPDA address
     */
    public void setJpdaAddress(final String jpdaAddress) {

        this.jpdaAddress = jpdaAddress;
    }

    /**
     * @return 'y' or 'n' for whether JVM should suspend execution immediately after startup when the "jpda start" command is
     *         executed
     * @see {@link #setJpdaSuspend(String)} for further details
     */
    public String getJpdaSuspend() {

        return jpdaSuspend;
    }

    /**
     * (Optional) Whether JVM should suspend execution immediately after startup when the "jpda start" command is executed.
     * Specifies whether JVM should suspend execution immediately after startup. Default is "n".
     *
     * @param jpdaSuspend if set must be either 'y' or 'n'
     */
    public void setJpdaSuspend(final String jpdaSuspend) {

        this.jpdaSuspend = jpdaSuspend;
    }

    /**
     * @return the complete JPDA Java runtime options used when the "jpda start" command is executed
     * @see {@link #setJpdaOpts(String)} for further details
     */
    public String getJpdaOpts() {

        return jpdaOpts;
    }

    /**
     * (Optional) Java runtime options used when the "jpda start" command is executed. If used, JPDA_TRANSPORT, JPDA_ADDRESS,
     * and JPDA_SUSPEND are ignored. Thus, all required JPDA options MUST be specified. The default is:
     *
     * "-agentlib:jdwp=transport=$JPDA_TRANSPORT,address=$JPDA_ADDRESS,server=y,suspend=$JPDA_SUSPEND"
     *
     * @param jpdaOpts the complete JPDA Java runtime options used when the "jpda start" command is executed
     */
    public void setJpdaOpts(final String jpdaOpts) {

        this.jpdaOpts = jpdaOpts;
    }

    /**
     * @return the logging config properties
     * @see {@link #setLoggingConfig(String)} for further details
     */
    public String getLoggingConfig() {

        return loggingConfig;
    }

    /**
     * (Optional) Override Tomcat's logging config file. Example (all one line)
     * "-Djava.util.logging.config.file=$CATALINA_BASE/conf/logging.properties".
     *
     * @param loggingConfig if set must be valid logging config properties
     */
    public void setLoggingConfig(final String loggingConfig) {

        this.loggingConfig = loggingConfig;
    }

    /**
     * @return the logging manager config property
     * @see {@link #setLoggingManager(String)} for further details
     */
    public String getLoggingManager() {

        return loggingManager;
    }

    /**
     * (Optional) Override Tomcat's logging manager. Example (all one line)
     * "-Djava.util.logging.manager=org.apache.juli.ClassLoaderLogManager".
     *
     * @param loggingManager if set must be a valid logging manager config property
     */
    public void setLoggingManager(final String loggingManager) {

        this.loggingManager = loggingManager;
    }

    public String getServerConfig() {

        return serverConfig;
    }

    public void setServerConfig(final String serverConfig) {

        this.serverConfig = serverConfig;
    }

    public int getStartupTimeoutInSeconds() {

        return startupTimeoutInSeconds;
    }

    public void setStartupTimeoutInSeconds(final int startupTimeoutInSeconds) {

        this.startupTimeoutInSeconds = startupTimeoutInSeconds;
    }

    public int getShutdownTimeoutInSeconds() {

        return shutdownTimeoutInSeconds;
    }

    public void setShutdownTimeoutInSeconds(final int shutdownTimeoutInSeconds) {

        this.shutdownTimeoutInSeconds = shutdownTimeoutInSeconds;
    }

    /**
     * @param outputToConsole the outputToConsole to set
     */
    public void setOutputToConsole(final boolean outputToConsole) {

        this.outputToConsole = outputToConsole;
    }

    /**
     * @return the outputToConsole
     */
    public boolean isOutputToConsole() {

        return outputToConsole;
    }

    /**
     * @deprecated use {@link #setCatalinaOpts(String)}, or less commonly {@link #setJavaOpts(String)} instead
     * @return the value of {@link #getCatalinaOpts()}
     */
    @Deprecated
    public String getJavaVmArguments() {

        return getCatalinaOpts();
    }

    /**
     * @deprecated use {@link #setCatalinaOpts(String)}, or less commonly {@link #setJavaOpts(String)} instead
     * @param javaVmArguments the value for {@link #setCatalinaOpts(String)}
     */
    @Deprecated
    public void setJavaVmArguments(final String javaVmArguments) {

        setCatalinaOpts(javaVmArguments);
    }

    /**
     * @deprecated use {@link #getLoggingConfig()} instead
     * @return the logging properties
     */
    @Deprecated
    public String getLoggingProperties() {

        return loggingProperties;
    }

    /**
     * @deprecated use {@link #setLoggingConfig(String)} instead
     * @param loggingProperties the logging config properties to set
     */
    @Deprecated
    public void setLoggingProperties(final String loggingProperties) {

        this.loggingProperties = loggingProperties;
    }

    /**
     * @deprecated no longer used
     * @return <code>null</code>
     */
    @Deprecated
    public String getWorkDir() {

        return null;
    }

    /**
     * @deprecated no longer used
     * @param workDir (ignored)
     */
    @Deprecated
    public void setWorkDir(final String workDir) {

    }

}
