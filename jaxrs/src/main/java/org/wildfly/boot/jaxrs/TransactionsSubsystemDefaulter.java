package org.wildfly.boot.jaxrs;

import org.wildfly.boot.container.AbstractSubsystemDefaulter;

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
