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
package org.jboss.arquillian.container.tomcat;

import java.io.File;
import java.io.IOException;

import org.jboss.arquillian.container.spi.ConfigurationException;

/**
 * Validate
 *
 * Validation utility
 *
 * @author <a href="mailto:aslak@conduct.no">Aslak Knutsen</a>
 * @version $Revision: $
 */
public final class Validate {
    private Validate() {
    }

    /**
     * Checks that object is not null, throws exception if it is.
     *
     * @param obj The object to check
     * @param message The exception message
     * @throws IllegalArgumentException Thrown if obj is null
     */
    public static void notNull(final Object obj, final String message) throws IllegalArgumentException {
        if (obj == null) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * Checks that the specified String is not null or empty, throws exception if it is.
     *
     * @param string The object to check
     * @param message The exception message
     * @throws IllegalArgumentException Thrown if obj is null
     */
    public static void notNullOrEmpty(final String string, final String message) throws IllegalArgumentException {
        if (string == null || string.length() == 0) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * Checks that the specified String is not null or empty.
     *
     * @param string The object to check
     * @return {@code true} if string is not empty, {@code false} otherwise
     */
    public static boolean isNotNullOrEmpty(final String string) throws IllegalArgumentException {
        if (string == null || string.length() == 0) {
            return false;
        }

        return true;
    }

    /**
     * Checks that obj is not null, throws exception if it is.
     *
     * @param obj The object to check
     * @param message The exception message
     * @throws IllegalStateException Thrown if obj is null
     */
    public static void stateNotNull(final Object obj, final String message) throws IllegalStateException {
        if (obj == null) {
            throw new IllegalStateException(message);
        }
    }

    /**
     * Checks that path represents a valid file
     *
     * @param path The path to file
     * @param message The exception message
     * @throws IllegalArgumentException Throws if given file does not exist or if it cannot be read
     */
    public static void isValidFile(final String path, final String message) throws IllegalArgumentException {
        notNull(path, message);
        File file = new File(path);

        if (!file.exists() || !file.canRead()) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * Checks that string is not null and not empty and it represents a path to a valid directory
     *
     * @param string The path to check
     * @param message The exception message
     * @throws ConfigurationException Thrown if string is empty, null or it does not represent a path the a valid directory
     */
    public static void configurationDirectoryExists(final String string, final String message) throws ConfigurationException {
        if (string == null || string.length() == 0 || new File(string).isDirectory() == false) {
            throw new ConfigurationException(message);
        }
    }

    /**
     * Checks if value lies in an interval (exclusive)
     * @param value the value
     * @param bottom the bottom boundary
     * @param top the top boundary
     * @param message the exception message
     * @throws IllegalArgumentException Thrown if value does not lie in the interval
     */
    public static void isInRange(final int value, final int bottom, final int top, final String message) throws IllegalArgumentException {
       if(value > top && value < bottom)
       {
          throw new IllegalArgumentException(message);
       }
    }

    public static void isInReadableDirectory(final String path, final String message) throws IllegalArgumentException {
        notNull(path, message);
        File file = new File(path);

        try {
            if (!file.createNewFile()) {
                if (!file.canRead()) {
                    throw new IllegalArgumentException(message);
                }
            }
        } catch (IOException e) {
            throw new IllegalArgumentException(message, e);
        }

    }

}
