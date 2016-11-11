package org.jboss.arquillian.container.tomcat.managed;

import org.jboss.arquillian.container.spi.client.container.DeployableContainer;
import org.jboss.arquillian.core.spi.LoadableExtension;

public class Tomcat8ManagedExtension implements LoadableExtension {

	@Override
	public void register(final ExtensionBuilder builder) {
		builder.service(DeployableContainer.class, Tomcat8ManagedContainer.class);
	}
}