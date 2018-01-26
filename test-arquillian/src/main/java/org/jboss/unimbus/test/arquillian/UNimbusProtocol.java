package org.jboss.unimbus.test.arquillian;

import java.util.Collection;

import org.jboss.arquillian.container.spi.client.protocol.ProtocolDescription;
import org.jboss.arquillian.container.spi.client.protocol.metadata.ProtocolMetaData;
import org.jboss.arquillian.container.test.spi.ContainerMethodExecutor;
import org.jboss.arquillian.container.test.spi.client.deployment.DeploymentPackager;
import org.jboss.arquillian.container.test.spi.client.protocol.Protocol;
import org.jboss.arquillian.container.test.spi.command.CommandCallback;
import org.jboss.unimbus.UNimbus;

/**
 * Created by bob on 1/25/18.
 */
public class UNimbusProtocol implements Protocol<UNimbusProtocolConfiguration> {

    public static final ProtocolDescription DESCRIPTION = new ProtocolDescription(UNimbus.PROJECT_NAME);

    @Override
    public Class<UNimbusProtocolConfiguration> getProtocolConfigurationClass() {
        return UNimbusProtocolConfiguration.class;
    }

    @Override
    public ProtocolDescription getDescription() {
        return DESCRIPTION;
    }

    @Override
    public DeploymentPackager getPackager() {
        return new UNimbusDeploymentPackager();
    }

    @Override
    public ContainerMethodExecutor getExecutor(UNimbusProtocolConfiguration config, ProtocolMetaData metadata, CommandCallback callback) {
        Collection<UNimbus> contexts = metadata.getContexts(UNimbus.class);
        return new UNimbusContainerMethodExecutor(contexts.iterator().next());
    }
}
