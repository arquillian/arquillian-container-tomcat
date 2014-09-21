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

import java.util.Map;

import org.jboss.arquillian.container.tomcat.StringUtils;

/**
 * A facade intended for use with a {@link ProcessBuilder#environment()} map.
 *
 * Note that as the <code>ProcessBuilder</code> API calls for the returned map to be mutated in order to affect the environment
 * of the launched process, this facade operates directly on the given map. In particular it assumes the map is modifiable.
 *
 * @author <a href="mailto:ian@ianbrandt.com">Ian Brandt</a>
 */
class EnvironmentFacade {

    private final Map<String, String> environmentMap;

    /**
     * This facade is intended to be used once for the given map.
     *
     * @param environmentMap the environment map to operate on.
     */
    EnvironmentFacade(final Map<String, String> environmentMap) {

        this.environmentMap = environmentMap;
    }

    /**
     * Clears the environment for the new process.
     */
    void clear() {

        environmentMap.clear();
    }

    /**
     * Puts the given environment variable if the given value is not blank
     *
     * @param environmentVariableName the environment variable name
     * @param environmentVariableValue the map environment variable value
     * @throws IllegalArgumentException if the <code>environmentVariableName</code> is null
     */
    void put(final String environmentVariableName, final String environmentVariableValue) throws IllegalArgumentException {

        assertEnvironmentVariableName(environmentVariableName);

        if (!StringUtils.isBlank(environmentVariableValue)) {

            environmentMap.put(environmentVariableName, environmentVariableValue);
        }
    }

    /**
     * Appends the given environment variable if the given value is not blank
     *
     * @param environmentVariableName the environment variable name
     * @param environmentVariableValue the environment variable value
     * @throws IllegalArgumentException if the <code>environmentVariableName</code> is null
     */
    void append(final String environmentVariableName, final String environmentVariableValue) throws IllegalArgumentException {

        assertEnvironmentVariableName(environmentVariableName);

        if (!StringUtils.isBlank(environmentVariableValue)) {

            final String originalValue = environmentMap.get(environmentVariableName);

            final String newValue =
                StringUtils.isBlank(originalValue) ? environmentVariableValue : originalValue + " " + environmentVariableValue;

            environmentMap.put(environmentVariableName, newValue);
        }
    }

    private void assertEnvironmentVariableName(final String environmentVariableName) throws IllegalArgumentException {

        if (environmentVariableName == null) {
            throw new IllegalArgumentException("environment variable name must not be null!");
        }
    }
}