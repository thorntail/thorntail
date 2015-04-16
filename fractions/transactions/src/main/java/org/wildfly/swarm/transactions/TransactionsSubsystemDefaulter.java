package org.wildfly.swarm.transactions;

import org.wildfly.swarm.container.AbstractSubsystemDefaulter;

/**
 * @author Bob McWhirter
 */
public class TransactionsSubsystemDefaulter extends AbstractSubsystemDefaulter<TransactionsSubsystem> {

    public TransactionsSubsystemDefaulter() {
        super(TransactionsSubsystem.class);
    }

    @Override
    public TransactionsSubsystem getDefaultSubsystem() throws Exception {
        return new TransactionsSubsystem(4712, 4713);
    }
}
