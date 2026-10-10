/*
 * JBoss, Home of Professional Open Source
 * Copyright 2026 Red Hat Inc. and/or its affiliates and other contributors
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

import java.util.Locale;

/**
 * The context path, version and name Tomcat derives from a deployment archive name.
 * <p>
 * Tomcat never takes the context path from the <code>path</code> attribute of a WAR bundled
 * "META-INF/context.xml"; that attribute is ignored and the path is inferred from the deployment name instead. A
 * "#" in the name denotes a path separator, and a "##" marks the start of a parallel deployment version, so
 * "foo#bar##1.0.war" is deployed to the context path "/foo/bar" as version "1.0".
 * <p>
 * This mirrors <code>org.apache.catalina.util.ContextName</code>, which the embedded container uses directly. The
 * managed and remote containers cannot depend on Catalina, hence this equivalent.
 *
 * @author Radoslav Husar
 * @see <a href="https://tomcat.apache.org/tomcat-10.1-doc/config/context.html">Tomcat Context Container</a>
 */
public final class ContextName {

    private static final String ROOT_NAME = "ROOT";

    private static final String VERSION_MARKER = "##";

    private static final String FWD_SLASH_REPLACEMENT = "#";

    private static final String WAR_EXTENSION = ".war";

    private final String path;

    private final String version;

    private final String name;

    /**
     * @param archiveName
     *     the deployment archive name, e.g. "foo#bar.war"
     */
    public ContextName(final String archiveName) {

        Validate.notNull(archiveName, "Archive name must not be null");

        String baseName = archiveName;

        // A name that is empty or starts with a version marker is the root context.
        if (baseName.isEmpty() || baseName.startsWith(VERSION_MARKER)) {
            baseName = ROOT_NAME + baseName;
        }

        if (baseName.toLowerCase(Locale.ENGLISH).endsWith(WAR_EXTENSION)) {
            baseName = baseName.substring(0, baseName.length() - WAR_EXTENSION.length());
        }

        final int versionIndex = baseName.indexOf(VERSION_MARKER);
        final String pathName;
        if (versionIndex > -1) {
            version = baseName.substring(versionIndex + VERSION_MARKER.length());
            pathName = baseName.substring(0, versionIndex);
        } else {
            version = "";
            pathName = baseName;
        }

        if (ROOT_NAME.equals(pathName)) {
            path = "";
        } else {
            path = "/" + pathName.replace(FWD_SLASH_REPLACEMENT, "/");
        }

        name = version.isEmpty() ? path : path + VERSION_MARKER + version;
    }

    /**
     * @return the context path, e.g. "/foo/bar", or an empty string for the root context
     */
    public String getPath() {

        return path;
    }

    /**
     * @return the parallel deployment version, or an empty string if the deployment is not versioned
     */
    public String getVersion() {

        return version;
    }

    /**
     * @return the context name, which is {@link #getPath()} suffixed with "##" and {@link #getVersion()} when the
     * deployment is versioned
     */
    public String getName() {

        return name;
    }

    @Override
    public String toString() {

        return name;
    }
}
