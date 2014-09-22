/*
 * JBoss, Home of Professional Open Source
 * Copyright 2014, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.arquillian.container.tomcat.managed;

import static org.jboss.arquillian.container.tomcat.managed.TomcatManagedConfiguration.*;

import java.io.File;
import java.util.Map;

import org.jboss.arquillian.container.tomcat.StringUtils;

/**
 * A strategy for transforming the container configuration to the Tomcat subprocess environment.
 *
 * @author <a href="mailto:ian@ianbrandt.com">Ian Brandt</a>
 */
class EnvironmentStrategy {

    /**
     * Mutates the given map with values from the given configuration.
     *
     * @param configuration the container configuration to apply
     * @param environmentMap the map of environment variables to be used by the Tomcat subprocess
     */
    void applyEnvironment(final TomcatManagedConfiguration configuration, final Map<String, String> environmentMap) {

        final EnvironmentMapFacade environment = new EnvironmentMapFacade(environmentMap);

        environment.put(CATALINA_HOME, configuration.getCatalinaHome());
        environment.put(CATALINA_BASE, configuration.getCatalinaBase());
        environment.put(CATALINA_OUT, configuration.getCatalinaOut());
        environment.put(CATALINA_PID, configuration.getCatalinaPid());
        environment.put(CATALINA_OPTS, configuration.getCatalinaOpts());
        environment.put(CATALINA_TMPDIR, configuration.getCatalinaTmpDir());
        environment.put(JAVA_HOME, configuration.getJavaHome());
        environment.put(JRE_HOME, configuration.getJreHome());
        environment.put(JAVA_OPTS, configuration.getJavaOpts());
        environment.put(JAVA_ENDORSED_DIRS, configuration.getJavaEndorsedDirs());
        environment.put(JPDA_TRANSPORT, configuration.getJpdaTransport());
        environment.put(JPDA_ADDRESS, configuration.getJpdaAddress());
        environment.put(JPDA_SUSPEND, configuration.getJpdaSuspend());
        environment.put(JPDA_OPTS, configuration.getJpdaOpts());
        environment.put(LOGGING_CONFIG, configuration.getLoggingConfig());
        environment.put(LOGGING_MANAGER, configuration.getLoggingManager());

        // TODO: Deprecate and remove remaining legacy configuration properties in favor of CATALINA_OPTS and LOGGING_CONFIG...

        environment.append(CATALINA_OPTS, "-Dcom.sun.management.jmxremote.port=" + configuration.getJmxPort());
        environment.append(CATALINA_OPTS, "-Dcom.sun.management.jmxremote.ssl=false");
        environment.append(CATALINA_OPTS, "-Dcom.sun.management.jmxremote.authenticate=false");

        @SuppressWarnings("deprecation")
        final String loggingProperties = configuration.getLoggingProperties();

        if (!StringUtils.isBlank(loggingProperties)) {

            environment.append(LOGGING_CONFIG, "-Djava.util.logging.config.file=.." + File.separator + "conf" + File.separator
                + loggingProperties);
        }
    }
}
