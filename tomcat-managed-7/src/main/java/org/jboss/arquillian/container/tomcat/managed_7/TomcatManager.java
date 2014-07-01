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
package org.jboss.arquillian.container.tomcat.managed_7;

import org.jboss.arquillian.container.tomcat.CommonTomcatManager;
import org.jboss.arquillian.container.tomcat.managed.CommonTomcatManagedConfiguration;

/**
 * Tomcat manager with different commands required for Tomcat 7 container
 *
 * @author <a href="mailto:kpiwko@redhat.com">Karel Piwko</a>
 *
 */
public class TomcatManager extends CommonTomcatManager<CommonTomcatManagedConfiguration>{

   public TomcatManager(CommonTomcatManagedConfiguration configuration)
   {
      super(configuration);
   }

   /* (non-Javadoc)
    * @see org.jboss.arquillian.container.tomcat.CommonTomcatManager#getDeployCommand()
    */
   @Override
   protected String getDeployCommand()
   {
      return "/text/deploy?path=";
   }

   /* (non-Javadoc)
    * @see org.jboss.arquillian.container.tomcat.CommonTomcatManager#getUndeployCommand()
    */
   @Override
   protected String getUndeployCommand()
   {
      return "/text/undeploy?path=";
   }

   /* (non-Javadoc)
    * @see org.jboss.arquillian.container.tomcat.CommonTomcatManager#getListCommand()
    */
   @Override
   protected String getListCommand()
   {
      return "/text/list";
   }

}
