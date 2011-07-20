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
package org.jboss.arquillian.container.tomcat.embedded_7;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

import org.apache.catalina.Container;
import org.apache.catalina.Context;
import org.apache.catalina.Host;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.startup.CatalinaProperties;
import org.apache.catalina.startup.ContextConfig;
import org.apache.catalina.startup.ExpandWar;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.startup.Tomcat.DefaultWebXmlListener;
import org.jboss.arquillian.container.spi.client.container.DeployableContainer;
import org.jboss.arquillian.container.spi.client.container.DeploymentException;
import org.jboss.arquillian.container.spi.client.container.LifecycleException;
import org.jboss.arquillian.container.spi.client.protocol.ProtocolDescription;
import org.jboss.arquillian.container.spi.client.protocol.metadata.HTTPContext;
import org.jboss.arquillian.container.spi.client.protocol.metadata.ProtocolMetaData;
import org.jboss.arquillian.container.spi.client.protocol.metadata.Servlet;
import org.jboss.arquillian.container.spi.context.annotation.DeploymentScoped;
import org.jboss.arquillian.core.api.InstanceProducer;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.descriptor.api.Descriptor;

/**
 * <p>Arquillian {@link DeployableContainer} implementation for an
 * Embedded Tomcat server; responsible for both lifecycle and deployment
 * operations.</p>
 *
 * <p>Please note that the context path set for the webapp must begin with
 * a forward slash. Otherwise, certain path operations within Tomcat
 * will behave inconsistently. Though it goes without saying, the host
 * name (bindAddress) cannot have a trailing slash for the same
 * reason.</p>
 *
 * @author <a href="mailto:jean.deruelle@gmail.com">Jean Deruelle</a>
 * @author Dan Allen
 * @author <a href="mailto:ian@ianbrandt.com">Ian Brandt</a>
 *
 * @see <a href='http://svn.apache.org/repos/asf/tomcat/trunk/test/org/apache/catalina/startup/TomcatBaseTest.java'>org.apache.catalina.startup.TomcatBaseTest</a>
 *
 * @version $Revision: $
 */
public class TomcatContainer implements DeployableContainer<TomcatConfiguration>
{
   private static final Logger log = Logger.getLogger(TomcatContainer.class.getName());

   /**
    * Tomcat container configuration
    */
   private TomcatConfiguration configuration;

   /**
    * Tomcat embedded
    */
   private Tomcat tomcat;

   private Host host;

   private File appBase;

   private boolean wasStarted;

   private final List<String> failedUndeployments = new ArrayList<String>();

   @Inject
   @DeploymentScoped
   private InstanceProducer<StandardContext> standardContextProducer;

   private final SystemPropertiesUtil systemPropertiesUtil = new SystemPropertiesUtil();

   public Class<TomcatConfiguration> getConfigurationClass()
   {
      return TomcatConfiguration.class;
   }

   public ProtocolDescription getDefaultProtocol()
   {
      return new ProtocolDescription("Servlet 3.0");
   }

   public void setup(TomcatConfiguration configuration)
   {
      this.configuration = configuration;
   }

   public void start() throws LifecycleException
   {
      /*
       * Derived from setUp() in
       * http://svn.apache.org/repos/asf/tomcat/tc7.0.x/tags/TOMCAT_7_0_16/test/org/apache/catalina/startup/TomcatBaseTest.java.
       */

      try
      {
         final File tempDir = getTomcatHomeFile();

         System.setProperty("catalina.base", tempDir.getAbsolutePath());
         // Trigger loading of catalina.properties
         CatalinaProperties.getProperty("foo");

         appBase = new File(tempDir, "webapps");
         if (!appBase.exists() && !appBase.mkdirs())
         {
            throw new LifecycleException("Unable to create appBase " + appBase.getAbsolutePath() + " for Tomcat");
         }

         tomcat = new Tomcat();
         tomcat.getService().setName(configuration.getServerName());
         tomcat.setHostname(configuration.getBindAddress());
         tomcat.setPort(configuration.getBindHttpPort());
         tomcat.setBaseDir(tempDir.getAbsolutePath());
         // Enable JNDI - it is disabled by default
         tomcat.enableNaming();

         tomcat.getEngine().setName(configuration.getServerName());

         host = tomcat.getHost();
         host.setAppBase(appBase.getAbsolutePath());

         /*
         if (configuration.isAccessLogEnabled())
         {
            AccessLogValve alv = new AccessLogValve();
            alv.setDirectory(tempDir.getAbsolutePath() + "/logs");
            alv.setPattern("%h %l %u %t \"%r\" %s %b %I %D");
            tomcat.getHost().getPipeline().addValve(alv);
         }
         */

         tomcat.start();
         wasStarted = true;
      }
      catch (Exception e)
      {
         throw new LifecycleException("Failed to start embedded Tomcat", e);
      }
   }

   public void stop() throws LifecycleException
   {
      try
      {
         removeFailedUnDeployments();
      }
      catch (Exception e)
      {
         throw new LifecycleException("Could not clean up", e);
      }

      if (wasStarted)
      {
         try
         {
            tomcat.stop();
         }
         catch (org.apache.catalina.LifecycleException e)
         {
            throw new LifecycleException("Failed to stop Tomcat", e);
         }
      }
   }

   public ProtocolMetaData deploy(final Archive<?> archive) throws DeploymentException
   {
      try
      {
         final File archiveFile = new File(appBase, archive.getName());

         archive.as(ZipExporter.class).exportTo(archiveFile, true);

         final String baseDir = getArchiveNameWithoutExtension(archive);
         final String contextPath = "/" + baseDir;

         final StandardContext standardContext =
         // (StandardContext) tomcat.addWebapp(null, contextPath, baseDir);
         (StandardContext) workAroundTomcat51526(contextPath, baseDir);

         standardContextProducer.set(standardContext);

         final HTTPContext httpContext = new HTTPContext(configuration.getBindAddress(),
               configuration.getBindHttpPort());

         for (String mapping : standardContext.findServletMappings())
         {
            httpContext.add(new Servlet(standardContext.findServletMapping(mapping), contextPath));
         }

         return new ProtocolMetaData().addContext(httpContext);
      }
      catch (Exception e)
      {
         throw new DeploymentException("Failed to deploy " + archive.getName(), e);
      }
   }

   /**
    * Used to work around <a href="https://issues.apache.org/bugzilla/show_bug.cgi?id=51526">Tomcat Bug 51526</a>.
    *
    * @param url the context path.
    * @param path the base dir.
    * @return the deployed context.
    * @throws MalformedURLException
    */
   private Context workAroundTomcat51526(final String url, final String path) throws MalformedURLException
   {
      /*
       * Derived from #addWebApp(Host, String, String, String) in
       * http://svn.apache.org/repos/asf/tomcat/tc7.0.x/tags/TOMCAT_7_0_16/java/org/apache/catalina/startup/Tomcat.java.
       */

      Context ctx = new StandardContext();
      ctx.setName(url);
      ctx.setPath(url);
      ctx.setDocBase(path);

      /*
       * Tomcat 7.0.19 will allow these Arquillian additions to be done via subclassing per
       * https://issues.apache.org/bugzilla/show_bug.cgi?id=51418.
       */
      ((StandardContext) ctx).setUnpackWAR(configuration.isUnpackArchive());
      ((StandardContext) ctx).setJ2EEServer("Arquillian-" + UUID.randomUUID().toString());

      /*
      if (defaultRealm == null) {
          initSimpleAuth();
      }
      standardContext.setRealm(defaultRealm);
       */

      ctx.addLifecycleListener(new DefaultWebXmlListener());

      ContextConfig ctxCfg = new EmbeddedContextConfig(); // Arquillian hook to add META-INF/context.xml processing
      ctx.addLifecycleListener(ctxCfg);

      // prevent it from looking ( if it finds one - it'll have dup error )
      ctxCfg.setDefaultWebXml("org/apache/catalin/startup/NO_DEFAULT_XML");

      // Let HostConfig listener do the deployment...
      tomcat.getHost().addChild(ctx);

      return ctx;
   }

   /* (non-Javadoc)
    * @see org.jboss.arquillian.container.spi.client.container.DeployableContainer#undeploy(org.jboss.shrinkwrap.api.Archive)
    */
   public void undeploy(final Archive<?> archive) throws DeploymentException
   {
      StandardContext standardContext = standardContextProducer.get();
      if (standardContext != null)
      {
         host.removeChild(standardContext);
         if (standardContext.getUnpackWAR())
         {
            deleteUnpackedWAR(standardContext);
         }
      }
   }

   private void undeploy(String name) throws DeploymentException
   {
      Container child = host.findChild(name);
      if (child != null)
      {
         host.removeChild(child);
      }
   }

   private void removeFailedUnDeployments() throws IOException
   {
      List<String> remainingDeployments = new ArrayList<String>();
      for (String name : failedUndeployments)
      {
         try
         {
            undeploy(name);
         }
         catch (Exception e)
         {
            IOException ioe = new IOException();
            ioe.initCause(e);
            throw ioe;
         }
      }
      if (remainingDeployments.size() > 0)
      {
         log.severe("Failed to undeploy these artifacts: " + remainingDeployments);
      }
      failedUndeployments.clear();
   }

   /* (non-Javadoc)
    * @see org.jboss.arquillian.spi.client.container.DeployableContainer#deploy(org.jboss.shrinkwrap.descriptor.api.Descriptor)
    */
   public void deploy(Descriptor descriptor) throws DeploymentException
   {
      throw new UnsupportedOperationException("Not implemented");
   }

   /* (non-Javadoc)
    * @see org.jboss.arquillian.spi.client.container.DeployableContainer#undeploy(org.jboss.shrinkwrap.descriptor.api.Descriptor)
    */
   public void undeploy(Descriptor descriptor) throws DeploymentException
   {
      throw new UnsupportedOperationException("Not implemented");
   }

   private File getTomcatHomeFile() throws LifecycleException
   {
      // TODO this needs to be a lot more robust
      String tomcatHome = configuration.getTomcatHome();
      File tomcatHomeFile;

      if (tomcatHome != null)
      {
         tomcatHomeFile = new File(systemPropertiesUtil.substituteEvironmentVariable(tomcatHome));

         if (!tomcatHomeFile.exists() && !tomcatHomeFile.mkdirs())
         {
            throw new LifecycleException("Unable to create home directory for Tomcat");
         }

         tomcatHomeFile.deleteOnExit();
         return tomcatHomeFile;
      }
      else
      {
         try
         {
            tomcatHomeFile = File.createTempFile("tomcat-embedded-7", null);
            if (!tomcatHomeFile.delete() || !tomcatHomeFile.mkdirs())
            {
               throw new LifecycleException("Unable to create temporary home directory "
                     + tomcatHomeFile.getAbsolutePath() + " for Tomcat");
            }
            tomcatHomeFile.deleteOnExit();
            return tomcatHomeFile;
         }
         catch (IOException e)
         {
            throw new LifecycleException("Unable to create temporary home directory for Tomcat", e);
         }
      }
   }

   private String getArchiveNameWithoutExtension(final Archive<?> archive)
   {
      final String archiveName = archive.getName();
      final int extensionOffset = archiveName.lastIndexOf('.');
      final String archiveNameWithoutExtension = extensionOffset >= 0
            ? archiveName.substring(0, extensionOffset)
            : archiveName;

      return archiveNameWithoutExtension;
   }

   /**
    * Make sure an the unpacked WAR is not left behind
    * you would think Tomcat would cleanup an unpacked WAR, but it doesn't
    */
   private void deleteUnpackedWAR(Context context)
   {
      File unpackDir = new File(host.getAppBase(), context.getPath().substring(1));
      if (unpackDir.exists())
      {
         ExpandWar.deleteDir(unpackDir);
      }
   }
}
