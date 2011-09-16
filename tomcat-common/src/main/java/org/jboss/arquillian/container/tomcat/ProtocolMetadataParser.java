/*
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
package org.jboss.arquillian.container.tomcat;

import java.io.IOException;
import java.net.URI;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.jboss.arquillian.container.spi.client.container.DeploymentException;
import org.jboss.arquillian.container.spi.client.protocol.metadata.HTTPContext;
import org.jboss.arquillian.container.spi.client.protocol.metadata.ProtocolMetaData;
import org.jboss.arquillian.container.spi.client.protocol.metadata.Servlet;

/**
 * @author <a href="kpiwko@redhat.com>Karel Piwko</a>
 *
 */
public class ProtocolMetadataParser<C extends CommonTomcatConfiguration>
{
   private static final Logger log = Logger.getLogger(ProtocolMetaData.class.getName());

   private C configuration;

   protected String catalinaServletTemplate = "Catalina:j2eeType=Servlet,WebModule=//%s/%s,*";

   public ProtocolMetadataParser(C configuration)
   {
      this.configuration = configuration;
   }

   /**
    * Retrieves given context's servlets information through JMX.
    *
    * How it works: 1) Get the WebModule, identified as //{host}/{contextPath} 2) Get it's path attrib 3) Get it's servlets
    * attrib, which is String[] which actually represents ObjectName[] 4) Get each of these Servlets and their mappings 5) For
    * each of {mapping}, do HTTPContext#add( new Servlet( "{mapping}", "//{host}/{contextPath}" ) );
    *
    * // WebModule -> ... -> Attributes // -> path == /manager // -> servlets == String[] // ->
    * Catalina:j2eeType=Servlet,name=<name>,WebModule=<...>,J2EEApplication =none,J2EEServer=none
    *
    *
    * @param context
    * @return
    * @throws DeploymentException
    */
   public ProtocolMetaData retrieveContextServletInfo(String context) throws DeploymentException
   {
      ProtocolMetaData protocolMetaData = new ProtocolMetaData();
      HTTPContext httpContext = new HTTPContext(configuration.getBindAddress(), configuration.getBindHttpPort());

      JMXConnector jmxc = null;
      try
      {
         jmxc = connect(configuration.getJmxUri());
      }
      catch (IOException ex)
      {
         throw new DeploymentException(
                      "Unable to contruct metadata for archive deployment.\n" +
                            "Can't connect to '" + configuration.getJmxUri() + "'."
                            + "\n   Make sure JMX remote acces is enabled Tomcat's JVM - e.g. in startup.sh using $JAVA_OPTS."
                            + "\n   Example (with no authentication):" + "\n     -Dcom.sun.management.jmxremote.port="
                            + configuration.getJmxPort() + "\n     -Dcom.sun.management.jmxremote.ssl=false"
                            + "\n     -Dcom.sun.management.jmxremote.authenticate=false", ex);
      }

      Set<ObjectInstance> servletMBeans;
      try
      {
         servletMBeans = getServletMBeans(jmxc, context);
      }
      catch (IOException e)
      {
         throw new DeploymentException("Unable to construct metadata for archive deployment", e);
      }

      // For each servlet MBean of the given context add the servlet info to the HTTPContext.
      for (ObjectInstance oi : servletMBeans)
      {
         String servletName = oi.getObjectName().getKeyProperty("name");
         httpContext.add(new Servlet(servletName, context));
         if (log.isLoggable(Level.FINE))
         {
            log.fine("Added servlet " + oi.toString() + " to HttpContext for archive" + context);
         }
      }

      protocolMetaData.addContext(httpContext);
      return protocolMetaData;
   }

   protected JMXConnector connect(URI jmxUri) throws IOException
   {
      log.info("Connecting to JMX at " + jmxUri);
      JMXServiceURL url = new JMXServiceURL(jmxUri.toASCIIString());

      return JMXConnectorFactory.connect(url, null);
   }

   protected Set<ObjectInstance> getServletMBeans(JMXConnector jmxc, String context) throws IOException
   {
      // connect to MBeanServer and get metadata
      MBeanServerConnection mbsc = jmxc.getMBeanServerConnection();
      String catalinaServlet = String.format(catalinaServletTemplate, configuration.getJmxVirtualHost(), context);

      ObjectName servletON;
      try
      {
         servletON = ObjectName.getInstance(catalinaServlet);
      }
      catch (MalformedObjectNameException e)
      {
         throw new IllegalArgumentException("Unable to retrieve catalina MBeans for protocol metadata construction.\n"
               + "Following object name is not valid: " + catalinaServlet, e);

      }
      catch (NullPointerException e)
      {
         throw new IllegalArgumentException("Unable to retrieve catalina MBeans for protocol metadata construction.\n"
               + "Object name must not be null", e);
      }

      // this might return empty set
      return mbsc.queryMBeans(servletON, null);
   }

   /**
    * catch (Exception ex)
    * {
    * throw new DeploymentException("Error listing context's '" + context + "' servlets and mappings: " + ex.toString(),
    * ex);
    * }
    * finally
    * {
    * if (jmxc != null)
    * {
    * try
    * {
    * jmxc.close();
    * }
    * catch (IOException ex)
    * {
    * log.severe(ex.getMessage());
    * }
    * }
    * }
    */
}
