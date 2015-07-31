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
package org.jboss.arquillian.container.tomcat.managed;

import org.jboss.arquillian.container.spi.client.container.DeployableContainer;
import org.jboss.arquillian.container.spi.client.protocol.ProtocolDescription;
import org.jboss.arquillian.container.tomcat.Tomcat7ManagerCommandSpec;

/**
 * <p>
 * Arquillian {@link DeployableContainer} implementation for an Managed Tomcat server; responsible for both lifecycle and
 * deployment operations.
 * </p>
 *
 * @author <a href="mailto:kpiwko@redhat.com">Karel Piwko</a>
 * @author <a href="mailto:jhuska@redhat.com">Juraj Huska</a>
 * @author <a href="mailto:ozizka@redhat.com">Ondrej Zizka</a>
 * @author <a href="mailto:dadrus@gmx.de">Dimitrij Drus</a>
 * @version $Revision: #1 $
 */
public class Tomcat8ManagedContainer extends TomcatManagedContainer {

	Tomcat8ManagedContainer() {
		super(new ProtocolDescription("Servlet 3.0"), new Tomcat7ManagerCommandSpec());
	}

}
