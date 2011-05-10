/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat Middleware LLC, and individual contributors
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

import java.io.File;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import javax.management.ObjectInstance;
import javax.xml.xpath.XPathExpressionException;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.WebResource.Builder;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import java.io.IOException;
import java.net.ConnectException;
import java.util.Hashtable;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import javax.ws.rs.core.MediaType;

import org.jboss.arquillian.spi.client.container.DeployableContainer;
import org.jboss.arquillian.spi.client.container.DeploymentException;
import org.jboss.arquillian.spi.client.container.LifecycleException;
import org.jboss.arquillian.spi.client.protocol.ProtocolDescription;
import org.jboss.arquillian.spi.client.protocol.metadata.HTTPContext;
import org.jboss.arquillian.spi.client.protocol.metadata.ProtocolMetaData;
import org.jboss.arquillian.spi.client.protocol.metadata.Servlet;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.descriptor.api.Descriptor;

/**
 * <p>Arquillian {@link DeployableContainer} implementation for an
 * Remote Tomcat server; responsible for both deployment operations.</p>
 *
 * 
 * @author <a href="mailto:ozizka@redhat.com">Ondrej Zizka</a>
 * @version $Revision: $
 */
public class TomcatRemoteContainer implements DeployableContainer<TomcatRemoteConfiguration>
{
   private static final Logger log = Logger.getLogger(TomcatRemoteContainer.class.getName());

   
   private static final String TMPDIR_SYS_PROP = "java.io.tmpdir";
   
   private static final String URL_PATH_DEPLOY = "/deploy";
   
   private static final String URL_PATH_UNDEPLOY = "/undeploy";
   
   private static final String URL_PATH_LIST = "/list";
   
   

   /**
    * Tomcat container configuration
    */
   private TomcatRemoteConfiguration conf;
   
   private String adminBaseUrl;

   private String deploymentName;

   private boolean wasStarted;

   private final List<String> failedUndeployments = new ArrayList<String>();

   public Class<TomcatRemoteConfiguration> getConfigurationClass()
   {
      return TomcatRemoteConfiguration.class;
   }

   public ProtocolDescription getDefaultProtocol()
   {
      return new ProtocolDescription("Servlet 2.5");
   }
   
   @Override
   public void setup(TomcatRemoteConfiguration configuration)
   {
      this.conf = configuration;
      
      this.adminBaseUrl = String.format("http://%s:%s@%s:%d/manager",
              this.conf.getUser(), this.conf.getPass(), this.conf.getHost(), this.conf.getHttpPort() );
      
      // StringUtils.substringBefore(archive.getName(), ".")
   }

   @Override
   public void start() throws LifecycleException
   {
      // TODO: Check that Tomcat is running.
   }

   @Override
   public void stop() throws LifecycleException
   {
      // TODO: Shutdown on :8005?
   }

   /* (non-Javadoc)
    * @see org.jboss.arquillian.spi.client.container.DeployableContainer#deploy(org.jboss.shrinkwrap.descriptor.api.Descriptor)
    */
   @Override
   public void deploy(Descriptor descriptor) throws DeploymentException
   {
      throw new UnsupportedOperationException("Not implemented");      
   }
   
   /* (non-Javadoc)
    * @see org.jboss.arquillian.spi.client.container.DeployableContainer#undeploy(org.jboss.shrinkwrap.descriptor.api.Descriptor)
    */
   @Override
   public void undeploy(Descriptor descriptor) throws DeploymentException
   {
      throw new UnsupportedOperationException("Not implemented");      
   }

   
   /**
    * Deploys to remote Tomcat using it's /manager web-app's org.apache.catalina.manager.ManagerServlet.
    * @param archive
    * @return
    * @throws DeploymentException 
    */
    @Override
    public ProtocolMetaData deploy(Archive<?> archive) throws DeploymentException {
        if (archive == null) {
            throw new IllegalArgumentException("archive must not be null");
        }

        final String archiveName = archive.getName();

        try {
            // Export to a file so we can send it over the wire
            final File archiveFile = new File(new File(System.getProperty("java.io.tmpdir")), archiveName);
            archive.as(ZipExporter.class).exportZip(archiveFile, true);

            // Split the suffix to get deployment.
            final String name = archiveName.substring(0, archiveName.lastIndexOf("."));
            this.deploymentName = name;

            Builder builder = prepareClientWebResource(URL_PATH_DEPLOY)
                    // Context path.
                    .queryParam("path", "/"+name)
                    .accept(MediaType.TEXT_PLAIN_TYPE)
                    .type(MediaType.APPLICATION_OCTET_STREAM_TYPE);
            
            
            final String reply = builder.put(String.class, archiveFile);
                    

            try {
                if (!isCallSuccessful(reply))
                    throw new DeploymentException("Deploy failed, Tomcat says: "+reply);
            } catch (Exception ex) {
                if( ex instanceof DeploymentException ) 
                    throw ex;
                else
                    throw new DeploymentException("Error parsing Tomcat's deploy response.", ex);
            }

            // Call has been successful, now we need another call to get the list of servlets
            final String subComponentsResponse = prepareClientWebResource(URL_PATH_LIST).get(String.class);
            //return this.parseForProtocolMetaData(subComponentsResponse);
            return this.retrieveContextServletInfo(name);
        }
        catch( Exception ex ) {
            throw new DeploymentException("Error in creating / deploying archive", ex);
        }
    }// deploy()

    @Override
    public void undeploy(final Archive<?> archive) throws DeploymentException
    {
        // Split the suffix to get deployment.
        final String archiveName = archive.getName();
        final String name = archiveName.substring(0, archiveName.lastIndexOf("."));
        //this.deploymentName = name;
        
        String reply = prepareClientWebResource(URL_PATH_UNDEPLOY)
             // Context path.
             .queryParam("path", "/"+name)
             .accept(MediaType.TEXT_PLAIN_TYPE)
             .get( String.class );
                
        try {
            if (!isCallSuccessful(reply)) {
                throw new DeploymentException("Undeploy failed, Tomcat says: "+reply);
            }
        } catch (Exception e) {
            throw new DeploymentException("Error parsing Tomcat's undeploy response.", e);
        }
    }// undeploy()

  
    /**
     * Basic REST call preparation
     *
     * @return the resource builder to execute
     */
    private WebResource prepareClient() {
        return prepareClientWebResource("");
    }

    /**
     * Basic REST call preparation, with the additional resource url appended
     *
     * @param additionalResourceUrl url portion past the base to use
     * @return the resource builder to execute
     */
    private WebResource prepareClientWebResource(String additionalResourceUrl) {
            // HTTP Client
            final Client client = Client.create();
            // Auth
            client.addFilter( new HTTPBasicAuthFilter( this.conf.getUser(), this.conf.getPass()) );
            WebResource resource = client.resource( this.adminBaseUrl + additionalResourceUrl );
            return resource;
    }
    
    
    
    /**
     * Looks for a successful exit code given the response of the call
     *
     * @param textResponse XML response from the REST call
     * @return true if call was successful, false otherwise
     * @throws XPathExpressionException if the xpath query could not be executed
     */
    private boolean isCallSuccessful(String textResponse) {
        // OK - Deployed application at context path /debug
        // OK - Undeployed application at context path /debug
        return textResponse.contains("OK");
    }

    

   /**
     * Parses output of /manager/list
      OK - Listed applications for virtual host localhost
      /:running:0:ROOT
      /manager:running:1:manager
      /docs:running:0:docs
      /examples:running:0:examples
      /host-manager:running:0:host-manager
     * 
     * @deprecated  /manager/list doesn't provide Serlvets and Mappings info -> we need JMX .
   */
    private ProtocolMetaData parseForProtocolMetaData(String textResponse) throws XPathExpressionException {
        final ProtocolMetaData protocolMetaData = new ProtocolMetaData();
        final HTTPContext httpContext = new HTTPContext(this.conf.getHost(), this.conf.getHttpPort());
        
        //BufferedInputStream bis = new BufferedInputStream( new ByteArrayInputStream( textResponse.getBytes()) );
        String[] lines = textResponse.split("\\n");

        for (int i = 1; i < lines.length; i++) {
            String line = lines[i];
            String[] parts = line.split(":");
            if( parts.length < 4 ) continue;
            httpContext.add(new Servlet(parts[3], parts[0]));
        }

        protocolMetaData.addContext(httpContext);
        return protocolMetaData;
    }

    
   

    protected void startTomcatRemote() throws UnknownHostException, org.apache.catalina.LifecycleException
    {
    }

    protected void stopTomcatRemote() throws LifecycleException, org.apache.catalina.LifecycleException
    {
    }
    
    
    
    /**
     * Retrieves given context's servlets information through JMX.
     * 
     * How it works:
     *   1)  Get the WebModule, identified as //{host}/{contextPath}
     *   2)  Get it's path attrib
     *   3)  Get it's servlets attrib, which is String[] which actually represents ObjectName[]
     *   4)  Get each of these Servlets and their mappings
     *   5)  For each of {mapping},  do HTTPContext#add( new Servlet( "{mapping}", "//{host}/{contextPath}" ) );
     * 
        // WebModule -> ... -> Attributes 
        //     -> path == /manager
        //     -> servlets == String[]
        //           -> Catalina:j2eeType=Servlet,name=<name>,WebModule=<...>,J2EEApplication=none,J2EEServer=none
     * 
     * 
     * @param context
     * @return
     * @throws DeploymentException 
     */
    protected ProtocolMetaData retrieveContextServletInfo( String context ) throws DeploymentException
    {
        JMXConnector jmxc = null;
        try{
            final ProtocolMetaData protocolMetaData = new ProtocolMetaData();
            final HTTPContext httpContext = new HTTPContext(this.conf.getHost(), this.conf.getHttpPort());
        
            // Create an RMI connector client and connect it to the RMI connector server
            // "service:jmx:rmi:///jndi/rmi://localhost:9999/server"
            //JMXServiceURL url = new JMXServiceURL( "service:jmx:rmi:///jndi/rmi:", this.conf.getHost(), this.conf.getJmxPort() );
            //String urlStr = String.format("service:jmx:rmi:///jndi/rmi://%s:%d/%s", this.conf.getHost(), this.conf.getJmxPort(), context);
            String urlStr = String.format("service:jmx:rmi:///jndi/rmi://%s:%d/jmxrmi", this.conf.getHost(), this.conf.getJmxPort());
            //String urlStr = String.format("service:jmx:rmi://%s/jndi/rmi://%s:%d/jmxrmi", this.conf.getHost(), this.conf.getHost(), this.conf.getJmxPort());
            
            log.info("Connecting to JMX: " + urlStr);
            JMXServiceURL url = new JMXServiceURL( urlStr );
            
            // Can we connect?
            try {
                jmxc = JMXConnectorFactory.connect(url, null);
            }
            catch ( ConnectException ex ){
                throw new ConnectException("Can't connect to '"+urlStr+"'."
                        + "\n   Make sure JMX props are set up for Tomcat's JVM - e.g. in startup.sh using $JAVA_OPTS."
                        + "\n   Example (with no authentication):"
                        + "\n     -Dcom.sun.management.jmxremote.port="+this.conf.getJmxPort()
                        + "\n     -Dcom.sun.management.jmxremote.ssl=false"
                        + "\n     -Dcom.sun.management.jmxremote.authenticate=false");
            }

            MBeanServerConnection mbsc = jmxc.getMBeanServerConnection();

            /// Debug
            String domains[] = mbsc.getDomains();
            for (int i = 0; i < domains.length; i++) {
                echo("\tDomain[" + i + "] = " + domains[i]);
            }
            
            if( false ){
                ///----- Not used - we can get servlets directly.
                // "Catalina:j2eeType=WebModule,name=//localhost/examples,J2EEApplication=none,J2EEServer=none"
                Hashtable<String, String> params = new Hashtable<String, String>();
                params.put("j2eeType", "WebModule");
                params.put("name", "//localhost/" + context);
                //mbsc.getMBeanInfo(ObjectName.getInstance("Catalina", params));
                ObjectName contextON = ObjectName.getInstance("Catalina", params);
                Set<ObjectInstance> contextMBeans = mbsc.queryMBeans( contextON,  null);
                if( contextMBeans.size() == 0 )
                    throw new DeploymentException("Context MBean not found: " + contextON);
                if( contextMBeans.size() > 1 )
                    log.warning( "More than one MBean found (taking first): " + contextON.toString() );
                ObjectInstance contextMBean = contextMBeans.iterator().next();
                //BaseModelMBean

                ObjectName[] children = (ObjectName[]) mbsc.getAttribute( contextMBean.getObjectName(), "children" );
                for (int i = 0; i < children.length; i++) {
                    ObjectName objectName = children[i];
                    echo( objectName.toString() );
                }
                ///-----
            }
            
            
            
            final String virtualHost = "localhost"; // TODO: Can be other virt host.
            
            // Construct the MBean query string.
            // Catalina:j2eeType=Servlet,name=Manager,WebModule=//localhost/manager,J2EEApplication=none,J2EEServer=none
            String jmxWebModuleName = "//"+virtualHost+"/" + context;
            ObjectName servletON = ObjectName.getInstance("Catalina:j2eeType=Servlet,WebModule=" + jmxWebModuleName + ",*");
            //ObjectName servletON = ObjectName.getInstance("Catalina:j2eeType=Servlet,*"); /// DEBUG - list all servlets of all modules (contexts).
            
            Set<ObjectInstance> servletMBeans = mbsc.queryMBeans( servletON,  null);
            if( servletMBeans.size() == 0 )
                throw new DeploymentException("No Servlet MBeans found for: " + servletON);
            
            
            // For each servlet MBean of the given context
            // add the servlet info to the HTTPContext.
            for( ObjectInstance oi : servletMBeans ) {
                String servletName = oi.getObjectName().getKeyProperty("name");
                log.fine("  Servlet: " + oi.toString());
                httpContext.add( new Servlet( servletName, context) );
                
                /*String[] mappings = (String[]) mbsc.invoke(oi.getObjectName(), "findMappings", new Object[0], new String[0]);
                for (String mapping : mappings) {
                    httpContext.add( new Servlet( mapping, contextFullPath) );
                }*/
            }
            
            protocolMetaData.addContext(httpContext);
            return protocolMetaData;
        }
        catch( Exception ex ){
            throw new DeploymentException("Error listing context's '"+context+"' servlets and mappings: "+ex.toString(), ex);
        }
        finally {
            if( jmxc != null )
                try { jmxc.close(); } catch (IOException ex){ /**/ }
        }

    }

    private void echo(String string) {
        System.out.println(string);
    }
    

}// class
