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

import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;

import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.descriptor.api.Descriptor;

/**
 * ShrinkWrapUtil
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 */
public final class ShrinkWrapUtil {

    private ShrinkWrapUtil() {

    }

    /**
     * Creates a tmp folder and exports the file. Returns the URL for that file location.
     *
     * @param archive
     *     Archive to export
     */
    public static URL toURL(final Archive<?> archive) {

        // create a random named temp file, then delete and use it as a directory
        try {
            final File root = File.createTempFile("arquillian", archive.getName());
            root.delete();
            root.mkdirs();

            final File deployment = new File(root, archive.getName());
            deployment.deleteOnExit();
            archive.as(ZipExporter.class).exportTo(deployment, true);
            return deployment.toURI().toURL();
        } catch (final Exception e) {
            throw new RuntimeException("Could not export deployment to temp", e);
        }
    }

    public static URL toURL(final Descriptor descriptor) {

        // create a random named temp file, then delete and use it as a directory
        try {
            final File root = File.createTempFile("arquillian", descriptor.getDescriptorName());
            root.delete();
            root.mkdirs();

            final File deployment = new File(root, descriptor.getDescriptorName());
            deployment.deleteOnExit();

            final FileOutputStream stream = new FileOutputStream(deployment);
            try {
                descriptor.exportTo(stream);
            } finally {
                try {
                    stream.close();
                } catch (final Exception e) {
                    throw new RuntimeException(e);
                }
            }

            return deployment.toURI().toURL();
        } catch (final Exception e) {
            throw new RuntimeException("Could not export deployment to temp", e);
        }
    }
}
