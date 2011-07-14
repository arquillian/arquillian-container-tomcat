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
import java.util.logging.Logger;

import org.apache.catalina.Container;
import org.apache.catalina.core.StandardContext;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.Assignable;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;

/**
 * Tomcat's {@link StandardContext} backed by a ShrinkWrap
 * {@link Archive}; capable of being deployed into
 * the Tomcat Embedded container
 *
 * @author <a href="mailto:andrew.rubinger@jboss.org">ALR</a>
 * @author Dan Allen
 * @author <a href="mailto:ian@ianbrandt.com">Ian Brandt</a>
 * @version $Revision: $
 */
public class ShrinkWrapStandardContext extends StandardContext implements Assignable
{

   //-------------------------------------------------------------------------------------||
   // Class Members ----------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   private static final long serialVersionUID = 1L;

   /**
    * Logger
    */
   private static final Logger log = Logger.getLogger(ShrinkWrapStandardContext.class.getName());

   /**
    *  /
    */
   private static final char ROOT = '/';

   //-------------------------------------------------------------------------------------||
   // Instance Members -------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * Underlying delegate
    */
   private final Archive<?> archive;

   private final String baseName;

   //-------------------------------------------------------------------------------------||
   // Constructor ------------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * Creates a new {@link ShrinkWrapStandardContext} using the
    * specified underlying archive
    *
    * @throws IllegalArgumentException If the archive is not specified
    */
   public ShrinkWrapStandardContext(final Archive<?> archive) throws IllegalArgumentException
   {
      // Invoke super
      super();

      // Precondition checks
      if (archive == null)
      {
         throw new IllegalArgumentException("archive must be specified");
      }

      // Remember the archive from which we're created
      this.archive = archive;

      final String archiveName = archive.getName();
      final int extensionOffset = archiveName.lastIndexOf('.');
      baseName = extensionOffset >= 0 ? archiveName.substring(0, extensionOffset) : archiveName;

      // context path must begin with a /
      this.setPath(ROOT + baseName);

      // we want to be as efficient as possible, so disable unpack, save config or cache by default
      this.setUnpackWAR(false);
      this.setSaveConfig(false);
      this.setCachingAllowed(false);
   }

   @Override
   public void setParent(Container container)
   {
      super.setParent(container);

      // Flush to file
      final File exported = new File(getAppBase() + File.separator + baseName + ".war");

      // We are overwriting the temporary file placeholder reserved by File#createTemplateFile()
      archive.as(ZipExporter.class).exportTo(exported, true);

      // Mark to delete when we come down
      exported.deleteOnExit();

      // Add the context
      log.info("Webapp archive location: " + exported.getAbsolutePath());
      this.setDocBase(exported.getAbsolutePath());
   }

   //-------------------------------------------------------------------------------------||
   // Required Implementations -----------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * {@inheritDoc}
    * @see org.jboss.shrinkwrap.api.Assignable#as(java.lang.Class)
    */
   @Override
   public <TYPE extends Assignable> TYPE as(final Class<TYPE> clazz)
   {
      return archive.as(clazz);
   }
}
