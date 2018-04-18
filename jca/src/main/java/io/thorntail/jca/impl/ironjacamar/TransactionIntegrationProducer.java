package io.thorntail.jca.impl.ironjacamar;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.transaction.TransactionManager;
import javax.transaction.TransactionSynchronizationRegistry;

import org.jboss.jca.core.spi.transaction.TransactionIntegration;
import org.jboss.jca.core.tx.jbossts.TransactionIntegrationImpl;
import org.jboss.tm.JBossXATerminator;
import org.jboss.tm.XAResourceRecoveryRegistry;
import org.jboss.tm.usertx.UserTransactionRegistry;

/**
 * Created by bob on 1/31/18.
 */
@ApplicationScoped
public class TransactionIntegrationProducer {

    @Produces
    TransactionIntegration transactionIntegration() {
        return new TransactionIntegrationImpl(
                this.tm,
                this.tsr,
                this.utr,
                this.terminator,
                this.rr
        );
    }

    @Inject
    TransactionManager tm;

    @Inject
    TransactionSynchronizationRegistry tsr;

    @Inject
    UserTransactionRegistry utr;

    @Inject
    JBossXATerminator terminator;

    @Inject
    XAResourceRecoveryRegistry rr;

}
