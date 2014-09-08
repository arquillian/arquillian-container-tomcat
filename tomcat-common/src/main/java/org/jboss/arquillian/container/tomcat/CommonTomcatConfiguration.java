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

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.jboss.arquillian.container.spi.ConfigurationException;
import org.jboss.arquillian.container.spi.client.container.ContainerConfiguration;

/**
 * Arquillian Tomcat Container Configuration
 *
 * @author <a href="mailto:kpiwko@redhat.com">Karel Piwko</a>
 * @author <a href="mailto:jhuska@redhat.com">Juraj Huska</a>
 * @version $Revision: $
 */
public class CommonTomcatConfiguration implements ContainerConfiguration
{

   private static final int MIN_PORT = 0;

   private static final int MAX_PORT = 65535;

   private String bindAddress = "localhost";

   private int bindHttpPort = 8080;

   private String user;

   private String pass;

   private int jmxPort = 8089;

   private int jmxServerPort = 0;

   private String jmxVirtualHost = "localhost";

   private String urlCharset = "ISO-8859-1";

   private String appBase = "webapps";

   private boolean unpackArchive = false;

   private URI jmxUri;

   private URL managerUrl;

   @Override
   public void validate() throws ConfigurationException
   {
      Validate.notNullOrEmpty(bindAddress, "Bind address must not be null or empty");
      Validate.isInRange(jmxPort, 0, MAX_PORT, "JMX port must be in interval ]" + MIN_PORT + "," + MAX_PORT
            + "[, but was " + jmxPort);
      Validate.isInRange(jmxServerPort, 0, MAX_PORT, "JMX server port must be in interval ]" + MIN_PORT + ","
            + MAX_PORT + "[, but was " + jmxServerPort);

      this.jmxUri = createJmxUri();
      this.managerUrl = createManagerUrl();
   }

   public String getBindAddress()
   {
      return bindAddress;
   }

   public void setBindAddress(final String bindAddress)
   {
      this.bindAddress = bindAddress;
   }

   public int getBindHttpPort()
   {
      return bindHttpPort;
   }

   /**
    * Set the HTTP bind port.
    *
    * @param bindHttpPort HTTP bind port
    */
   public void setBindHttpPort(final int bindHttpPort)
   {
      this.bindHttpPort = bindHttpPort;
   }

   public String getUser()
   {
      return user;
   }

   public void setUser(final String user)
   {
      this.user = user;
   }

   public String getPass()
   {
      return pass;
   }

   public void setPass(final String pass)
   {
      this.pass = pass;
   }

   public int getJmxPort()
   {
      return jmxPort;
   }

   public void setJmxPort(final int jmxPort)
   {
      this.jmxPort = jmxPort;
   }

   public int getJmxServerPort()
   {
      return jmxServerPort;
   }

   public void setJmxServerPort(final int jmxServerPort)
   {
      this.jmxServerPort = jmxServerPort;
   }

   public String getAppBase()
   {
      return appBase;
   }

   /**
    * @param appBase the directory where the deployed webapps are stored within the Tomcat installation
    */
   public void setAppBase(final String appBase)
   {
      this.appBase = appBase;
   }

   /**
    * @return a switch indicating whether the WAR should be unpacked
    */
   public boolean isUnpackArchive()
   {
      return unpackArchive;
   }

   /**
    * Sets the WAR to be unpacked into the java.io.tmpdir when deployed. Unpacking is required if you are using Weld to provide
    * CDI support in a servlet environment.
    *
    * @param unpackArchive a switch indicating whether the WAR should be unpacked
    */
   public void setUnpackArchive(final boolean unpackArchive)
   {
      this.unpackArchive = unpackArchive;
   }

   /**
    * @param urlCharset the urlCharset to set
    */
   public void setUrlCharset(final String urlCharset)
   {
      this.urlCharset = urlCharset;
   }

   /**
    * @return the urlCharset
    */
   public String getUrlCharset()
   {
      return urlCharset;
   }

   /**
    * @param jmxVirtualHost the jmxVirtualHost to set
    */
   public void setJmxVirtualHost(final String jmxVirtualHost)
   {
      this.jmxVirtualHost = jmxVirtualHost;
   }

   /**
    * @return the jmxVirtualHost
    */
   public String getJmxVirtualHost()
   {
      return jmxVirtualHost;
   }

   /**
    * @return the jmxUri
    */
   public URI getJmxUri()
   {
      return jmxUri;
   }

   /**
    * @return the managerUrl
    */
   public URL getManagerUrl()
   {
      return managerUrl;
   }

   protected URI createJmxUri()
   {
      try
      {
         final String uriString;
         final String template;

         if (jmxServerPort != 0)
         {
            template = "service:jmx:rmi://%s:%d/jndi/rmi://%s:%d/jmxrmi";
            uriString = String.format(template, bindAddress, jmxServerPort, bindAddress, jmxPort);
         }
         else
         {
            template = "service:jmx:rmi:///jndi/rmi://%s:%d/jmxrmi";
            uriString = String.format(template, bindAddress, jmxPort);
         }

         return new URI(uriString);
      }
      catch (final URISyntaxException e)
      {
         throw new ConfigurationException("JMX URI is not valid, please provide ", e);
      }
   }

   protected URL createManagerUrl()
   {
      try
      {
         final String template = "http://%s:%d/manager";

         final String urlString = String.format(template, bindAddress, bindHttpPort);

         return new URL(urlString);
      }
      catch (final MalformedURLException e)
      {
         throw new ConfigurationException("Manager URL is not valid, please provide ", e);
      }
   }

}
