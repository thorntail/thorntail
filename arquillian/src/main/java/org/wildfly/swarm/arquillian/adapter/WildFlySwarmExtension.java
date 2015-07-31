package org.wildfly.swarm.arquillian.adapter;

import org.jboss.arquillian.container.spi.client.container.DeployableContainer;
import org.jboss.arquillian.core.spi.LoadableExtension;

/**
 * @author Bob McWhirter
 */
public class WildFlySwarmExtension implements LoadableExtension {
    @Override
    public void register(ExtensionBuilder builder) {
        builder.service(DeployableContainer.class, WildFlySwarmContainer.class);
    }
}
