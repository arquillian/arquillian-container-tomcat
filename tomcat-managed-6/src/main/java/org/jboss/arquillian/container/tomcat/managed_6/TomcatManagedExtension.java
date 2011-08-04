package org.jboss.arquillian.container.tomcat.managed_6;

import org.jboss.arquillian.container.spi.client.container.DeployableContainer;

import org.jboss.arquillian.core.spi.LoadableExtension;

public class TomcatManagedExtension implements LoadableExtension {

	 @Override
	   public void register(ExtensionBuilder builder)
	   {
	      builder.service(DeployableContainer.class, TomcatManagedContainer.class);
	   }
}
