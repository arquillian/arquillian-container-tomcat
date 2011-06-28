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
package org.jboss.arquillian.container.tomcat.remote_6;

import java.net.URI;
import java.net.URISyntaxException;

import org.jboss.arquillian.container.spi.ConfigurationException;
import org.jboss.arquillian.container.spi.client.container.ContainerConfiguration;

/**
 * Arquillian Tomcat Container Configuration
 * 
 * @author <a href="mailto:ozizka@redhat.com">Ondrej Zizka</a>
 * @version $Revision: $
 */
public class TomcatRemoteConfiguration implements ContainerConfiguration
{
   private static final int MAX_PORT = 65535;

   private String host = "localhost";

   private int httpPort = 8080;

   private String user;

   private String pass;

   private int jmxPort = 8089;

   private URI jmxUrl;

   /* (non-Javadoc)
    * @see org.jboss.arquillian.spi.client.container.ContainerConfiguration#validate()
    */
   @Override
   public void validate() throws ConfigurationException
   {
      if (this.jmxPort > MAX_PORT)
         throw new ConfigurationException("JMX port larger than " + MAX_PORT + ": " + this.jmxPort);

      try
      {
         this.jmxUrl = new URI("service:jmx:rmi:///jndi/rmi://" + this.host + ":" + this.jmxPort + "/jmxrmi");
      }
      catch (URISyntaxException ex)
      {
         throw new ConfigurationException(ex.getMessage(), ex);
      }
   }

   public String getPass()
   {
      return pass;
   }

   public void setPass(String pass)
   {
      this.pass = pass;
   }

   public String getUser()
   {
      return user;
   }

   public void setUser(String user)
   {
      this.user = user;
   }

   public String getHost()
   {
      return host;
   }

   public void setHttpPort(int httpPort)
   {
      this.httpPort = httpPort;
   }

   public void setHost(String host)
   {
      this.host = host;
   }

   public int getJmxPort()
   {
      return jmxPort;
   }

   public void setJmxPort(int jmxPort)
   {
      this.jmxPort = jmxPort;
   }

   public URI getJmxUrl()
   {
      return jmxUrl;
   }

   public int getHttpPort()
   {
      return httpPort;
   }
}
