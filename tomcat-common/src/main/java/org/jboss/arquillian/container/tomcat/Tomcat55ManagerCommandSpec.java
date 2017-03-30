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
package org.jboss.arquillian.container.tomcat;

/**
 * Commands known to work for Tomcat 5.5.x through 6.x.
 *
 * @author <a href="mailto:ian@ianbrandt.com">Ian Brandt</a>
 * @see <a href="http://tomcat.apache.org/tomcat-5.5-doc/manager-howto.html">Tomcat 5.5 Manager App HOW-TO</a>
 * @see <a href="http://tomcat.apache.org/tomcat-6.0-doc/manager-howto.html">Tomcat 6.0 Manager App HOW-TO</a>
 */
public class Tomcat55ManagerCommandSpec implements TomcatManagerCommandSpec {

    public String getServerInfoCommand() {
        return "/serverinfo";
    }

    public String getListCommand() {

        return "/list";
    }

    public String getDeployCommand() {

        return "/deploy?path=";
    }

    public String getUndeployCommand() {

        return "/undeploy?path=";
    }
}
