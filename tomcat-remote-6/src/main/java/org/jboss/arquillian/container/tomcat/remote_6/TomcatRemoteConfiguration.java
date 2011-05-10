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
import java.net.URL;
import org.jboss.arquillian.spi.ConfigurationException;
import org.jboss.arquillian.spi.client.container.ContainerConfiguration;

/**
 * Arquillian Tomcat Container Configuration
 * 
 * @author <a href="mailto:ozizka@redhat.com">Ondrej Zizka</a>
 * @version $Revision: $
 */
public class TomcatRemoteConfiguration implements ContainerConfiguration
{
   private static final int MAX_PORT = 65535;
   private static final String JMX_PROTOCOL = "service:jmx:rmi:";
    
   private String host = "localhost";

   private int httpPort = 8080;
   
   private String user;
   
   private String pass;
      
   private int jmxPort = 8089;
   
   private URI jmxUrl;

   private String serverName = "arquillian-tomcat-remote-6";
   
   private boolean unpackArchive = false;
   
   
    /* (non-Javadoc)
     * @see org.jboss.arquillian.spi.client.container.ContainerConfiguration#validate()
     */
    @Override
    public void validate() throws ConfigurationException {
        if(this.jmxPort > MAX_PORT)
            throw new ConfigurationException("JMX port larger than "+MAX_PORT+": "+this.jmxPort);
        
        try {
            this.jmxUrl = new URI("service:jmx:rmi:///jndi/rmi://"+this.host+":"+this.jmxPort+"/jmxrmi");
        } catch (URISyntaxException ex) {
            throw new ConfigurationException( ex.getMessage(), ex );
        }
    }

    
    
    
    public String getPass() {
        return pass;
    }

    public void setPass(String pass) {
        this.pass = pass;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

   

    public String getHost() {
        return host;
    }

    public void setHttpPort(int httpPort) {
        this.httpPort = httpPort;
    }

    public void setHost(String host) {
        this.host = host;
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

    /*public void setJmxUrl(URL jmxUrl) {
      if( ! JMX_PROTOCOL.equals(jmxUrl.getProtocol() ) )
         throw new ConfigurationException( "URL's protocol is not '"+JMX_PROTOCOL+"': "+jmxUrl.toString() );
      this.jmxUrl = jmxUrl;
      this.host = jmxUrl.getHost();
      this.jmxPort = jmxUrl.getPort();
    }*/


   
   
   
   
   
   public int getHttpPort()
   {
      return httpPort;
   }

   /**
    * Set the HTTP port.
    *
    * @param httpPort
    *            HTTP port
    */
   public void setBindHttpPort(int httpPort)
   {
      this.httpPort = httpPort;
   }

   /**
    * @param serverName the serverName to set
    */
   public void setServerName(String serverName)
   {
      this.serverName = serverName;
   }

   /**
    * @return the serverName
    */
   public String getServerName()
   {
      return serverName;
   }

   /**
    * @return a switch indicating whether the WAR should be unpacked
    */
   public boolean isUnpackArchive()
   {
      return unpackArchive;
   }

   /**
    * Sets the WAR to be unpacked into the java.io.tmpdir when deployed.
    * Unpacking is required if you are using Weld to provide CDI support
    * in a servlet environment.
    *
    * @param a switch indicating whether the WAR should be unpacked
    */
   public void setUnpackArchive(boolean unpack)
   {
      this.unpackArchive = unpack;
   }
}
