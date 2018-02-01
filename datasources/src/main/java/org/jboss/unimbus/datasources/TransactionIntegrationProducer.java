package org.jboss.unimbus.datasources;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.transaction.TransactionManager;
import javax.transaction.TransactionSynchronizationRegistry;

import com.arjuna.ats.internal.jta.transaction.arjunacore.TransactionSynchronizationRegistryImple;
import org.jboss.jca.core.spi.transaction.TransactionIntegration;
import org.jboss.jca.core.spi.transaction.recovery.XAResourceRecoveryRegistry;
import org.jboss.jca.core.spi.transaction.usertx.UserTransactionRegistry;
import org.jboss.jca.core.spi.transaction.xa.XATerminator;
import org.jboss.jca.core.tx.jbossts.XAResourceRecoveryRegistryImpl;
import org.jboss.jca.core.tx.noopts.TransactionIntegrationImpl;
import org.jboss.jca.core.tx.noopts.TransactionSynchronizationRegistryImpl;
import org.jboss.jca.core.tx.noopts.UserTransactionRegistryImpl;
import org.jboss.jca.core.tx.noopts.XATerminatorImpl;

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

    TransactionSynchronizationRegistry tsr = new TransactionSynchronizationRegistryImple();

    UserTransactionRegistry utr = new UserTransactionRegistryImpl();

    XATerminator terminator = new XATerminatorImpl();

    XAResourceRecoveryRegistry rr = null;

}
