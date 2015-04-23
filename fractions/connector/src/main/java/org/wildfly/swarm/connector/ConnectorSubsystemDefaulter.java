package org.wildfly.swarm.connector;

import org.wildfly.swarm.container.AbstractSubsystemDefaulter;

/**
 * @author Bob McWhirter
 */
public class ConnectorSubsystemDefaulter extends AbstractSubsystemDefaulter<ConnectorSubsystem> {

    public ConnectorSubsystemDefaulter() {
        super(ConnectorSubsystem.class);
    }

    @Override
    public ConnectorSubsystem getDefaultSubsystem() throws Exception {
        return new ConnectorSubsystem();
    }
}
