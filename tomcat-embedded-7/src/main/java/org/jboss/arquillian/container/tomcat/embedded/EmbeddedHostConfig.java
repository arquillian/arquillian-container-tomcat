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
package org.jboss.arquillian.container.tomcat.embedded;

import org.apache.catalina.startup.ContextConfig;
import org.apache.catalina.startup.HostConfig;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.startup.Tomcat.DefaultWebXmlListener;
import org.apache.catalina.util.ContextName;

/**
 * A custom {@link HostConfig} for use in the Embedded Tomcat container integration for Arquillian.
 * 
 * <p>
 * This configuration makes the protected Tomcat WAR deployment implementation (as is used by the typical standalone server)
 * available for embedded use. We do this to retain standard deployment features that are notably absent from the current
 * {@link Tomcat} deployment logic, for instance:
 * <ul>
 * <li>Deployment of an archive named "ROOT.war" to the default context "/".</li>
 * <li>Proper processing of "META-INF/context.xml" if present in the WAR.</li>
 * </ul>
 * </p>
 *
 * <p>
 * You'll very likely want to set the {@link ContextConfig} class for the associated host to an {@link EmbeddedContextConfig}
 * via <code>host.setConfigClass(EmbeddedContextConfig.class.getCanonicalName())</code>. (Note that
 * {@link HostConfig#getConfigClass()} is not currently used.) This will result in the application of context configuration
 * normally sourced from "$CATALINA_BASE/conf/web.xml". This is typically done by <code>Tomcat</code> via a
 * {@link DefaultWebXmlListener} added to the context, but <code>HostConfig</code> lacks a suitable hook to add such a listener
 * prior to the start life cycle.
 * </p>
 *
 * @author <a href="mailto:ian@ianbrandt.com">Ian Brandt</a>
 */
public class EmbeddedHostConfig extends HostConfig {

    /**
     * Deploy a WAR with the given file name to be found in the configured app base.
     *
     * @param warFileName the WAR file name, e.g. "ROOT.war".
     */
    public void deployWAR(final String warFileName) {

        final String contextName = getContextName(warFileName);

        deployWARs(appBase(), new String[] { warFileName });

        addServiced(contextName);
    }

    /**
     * Undeploy a WAR with the given file name.
     *
     * @param warFileName the WAR file name, e.g. "ROOT.war".
     */
    public void undeployWAR(final String warFileName) {

        final String contextName = getContextName(warFileName);

        unmanageApp(contextName);

        removeServiced(contextName);
    }

    private String getContextName(final String warFileName) {

        final ContextName contextName = CompatUtils.createContextName(warFileName);
        return contextName.getName();
    }
}
