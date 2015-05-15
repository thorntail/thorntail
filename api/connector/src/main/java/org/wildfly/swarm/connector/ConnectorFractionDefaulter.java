package org.wildfly.swarm.connector;

import org.wildfly.swarm.container.AbstractFractionDefaulter;

/**
 * @author Bob McWhirter
 */
public class ConnectorFractionDefaulter extends AbstractFractionDefaulter<ConnectorFraction> {

    public ConnectorFractionDefaulter() {
        super(ConnectorFraction.class);
    }

    @Override
    public ConnectorFraction getDefaultSubsystem() throws Exception {
        return new ConnectorFraction();
    }
}
