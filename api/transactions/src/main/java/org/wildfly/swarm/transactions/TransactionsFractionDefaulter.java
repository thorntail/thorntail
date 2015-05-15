package org.wildfly.swarm.transactions;

import org.wildfly.swarm.container.AbstractFractionDefaulter;

/**
 * @author Bob McWhirter
 */
public class TransactionsFractionDefaulter extends AbstractFractionDefaulter<TransactionsFraction> {

    public TransactionsFractionDefaulter() {
        super(TransactionsFraction.class);
    }

    @Override
    public TransactionsFraction getDefaultSubsystem() throws Exception {
        return new TransactionsFraction(4712, 4713);
    }
}
