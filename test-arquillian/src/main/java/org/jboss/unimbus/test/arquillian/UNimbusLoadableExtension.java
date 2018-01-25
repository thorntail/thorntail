package org.jboss.unimbus.test.arquillian;

import org.jboss.arquillian.container.spi.client.container.DeployableContainer;
import org.jboss.arquillian.container.test.spi.client.protocol.Protocol;
import org.jboss.arquillian.core.spi.LoadableExtension;

/**
 * Created by bob on 1/25/18.
 */
public class UNimbusLoadableExtension implements LoadableExtension {
    @Override
    public void register(ExtensionBuilder extensionBuilder) {
        extensionBuilder.service(DeployableContainer.class, UNimbusDeployableContainer.class);
        extensionBuilder.service(Protocol.class, UNimbusProtocol.class);
    }
}
