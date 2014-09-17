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
 * Commands known to work for Tomcat 7.x through 8.x.
 *
 * @author <a href="mailto:ian@ianbrandt.com">Ian Brandt</a>
 *
 * @see <a href="http://tomcat.apache.org/tomcat-7.0-doc/manager-howto.html">Tomcat 7.0 Manager App HOW-TO</a>
 * @see <a href="http://tomcat.apache.org/tomcat-8.0-doc/manager-howto.html">Tomcat 8.0 Manager App HOW-TO</a>
 */
public class Tomcat7ManagerCommandSpec implements TomcatManagerCommandSpec {

    @Override
    public String getListCommand() {

        return "/text/list";
    }

    @Override
    public String getDeployCommand() {

        return "/text/deploy?path=";
    }

    @Override
    public String getUndeployCommand() {

        return "/text/undeploy?path=";
    }
}
