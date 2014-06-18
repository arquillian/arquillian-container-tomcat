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
package org.jboss.arquillian.container.tomcat.managed_7;

import java.security.AccessController;
import java.security.PrivilegedAction;

import org.jboss.arquillian.container.spi.ConfigurationException;
import org.jboss.arquillian.container.tomcat.CommonTomcatConfiguration;
import org.jboss.arquillian.container.tomcat.Validate;

/**
 * Arquillian Tomcat Container Configuration
 *
 * @author <a href="mailto:kpiwko@redhat.com">Karel Piwko</a>
 * @author <a href="mailto:jhuska@redhat.com">Juraj Huska</a>
 * @version $Revision: $
 */
public class TomcatManagedConfiguration extends CommonTomcatConfiguration
{
   private boolean outputToConsole = true;

   private String catalinaHome = System.getenv("CATALINA_HOME");

   private String catalinaBase = System.getenv("CATALINA_BASE");

   private String javaHome = System.getenv("JAVA_HOME");

   private String javaVmArguments = "-Xmx512m -XX:MaxPermSize=128m";

   private int startupTimeoutInSeconds = 120;

   private int shutdownTimeoutInSeconds = 45;

   private String workDir = null;

   private String serverConfig = "server.xml";

   private String loggingProperties = "logging.properties";

   public TomcatManagedConfiguration() {
       // if no javaHome set, reuse this Java JVM
       if (javaHome == null || "".equals(javaHome)) {
           javaHome = System.getProperty("java.home");
       }
       if (catalinaBase == null || "".equals(catalinaBase)) {
           catalinaBase = catalinaHome;
       }
   }

   @Override
   public void validate() throws ConfigurationException
   {
      super.validate();

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
      this.setOutputToConsole(AccessController.doPrivileged(new PrivilegedAction<Boolean>()
      {
         @Override
         public Boolean run()
         {
            // By default, redirect to stdout unless disabled by this property
            String val = System.getProperty("org.apache.tomcat.writeconsole");
            return val == null || !"false".equals(val);
         }
      }));

   }

   public String getCatalinaHome()
   {
      return catalinaHome;
   }

   public void setCatalinaHome(String catalinaHome)
   {
      this.catalinaHome = catalinaHome;
   }

   public String getCatalinaBase() {
        return catalinaBase;
    }

   public void setCatalinaBase(String catalinaBase) {
        this.catalinaBase = catalinaBase;
    }

   public String getJavaHome()
   {
      return javaHome;
   }

   public void setJavaHome(String javaHome)
   {
      this.javaHome = javaHome;
   }

   public String getJavaVmArguments()
   {
      return javaVmArguments;
   }

   /**
    * This will override the default ("-Xmx512m -XX:MaxPermSize=128m") startup JVM arguments.
    *
    * @param javaVmArguments use as start up arguments
    */
   public void setJavaVmArguments(String javaVmArguments)
   {
      this.javaVmArguments = javaVmArguments;
   }

   public int getStartupTimeoutInSeconds()
   {
      return startupTimeoutInSeconds;
   }

   public void setStartupTimeoutInSeconds(int startupTimeoutInSeconds)
   {
      this.startupTimeoutInSeconds = startupTimeoutInSeconds;
   }

   public int getShutdownTimeoutInSeconds()
   {
      return shutdownTimeoutInSeconds;
   }

   public void setShutdownTimeoutInSeconds(int shutdownTimeoutInSeconds)
   {
      this.shutdownTimeoutInSeconds = shutdownTimeoutInSeconds;
   }

   public String getWorkDir()
   {
      return workDir;
   }

   /**
    * @param workDir the directory where the compiled JSP files and session serialization data is stored
    */
   public void setWorkDir(String workDir)
   {
      this.workDir = workDir;
   }

   public String getServerConfig()
   {
      return serverConfig;
   }

   public void setServerConfig(String serverConfig)
   {
      this.serverConfig = serverConfig;
   }

   /**
    * @return the loggingProperties
    */
   public String getLoggingProperties()
   {
      return loggingProperties;
   }

   /**
    * @param loggingProperties the loggingProperties to set
    */
   public void setLoggingProperties(String loggingProperties)
   {
      this.loggingProperties = loggingProperties;
   }

   /**
    * @param outputToConsole the outputToConsole to set
    */
   public void setOutputToConsole(boolean outputToConsole)
   {
      this.outputToConsole = outputToConsole;
   }

   /**
    * @return the outputToConsole
    */
   public boolean isOutputToConsole()
   {
      return outputToConsole;
   }

   /**
    * Use {@link TomcatManagedConfiguration#setOutputToConsole(boolean)} instead
    * @param writeOutputToConsole the writeOutputToConsole to set
    */
   @Deprecated
   public void setWriteOutputToConsole(boolean writeOutputToConsole)
   {
      this.setOutputToConsole(writeOutputToConsole);
   }

   /**
    * User {@link TomcatManagedConfiguration#isOutputToConsole()} instead
    * @return the writeOutputToConsole
    */
   @Deprecated
   public boolean isWriteOutputToConsole()
   {
      return isOutputToConsole();
   }

}
