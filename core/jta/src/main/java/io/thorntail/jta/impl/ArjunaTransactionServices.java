package io.thorntail.jta.impl;

import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.Synchronization;
import javax.transaction.SystemException;

import com.arjuna.ats.jta.TransactionManager;
import com.arjuna.ats.jta.UserTransaction;
import org.jboss.weld.transaction.spi.TransactionServices;

/**
 * Created by bob on 1/16/18.
 */
public class ArjunaTransactionServices implements TransactionServices {

    @Override
    public void cleanup() {

    }

    @Override
    public void registerSynchronization(Synchronization synchronization) {
        try {
            TransactionManager.transactionManager().getTransaction().registerSynchronization(synchronization);
        } catch (IllegalStateException e) {
            throw new RuntimeException(e);
        } catch (RollbackException e) {
            throw new RuntimeException(e);
        } catch (SystemException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean isTransactionActive() {
        try {
            int status = TransactionManager.transactionManager().getStatus();
            return status == Status.STATUS_ACTIVE ||
                    status == Status.STATUS_COMMITTING ||
                    status == Status.STATUS_MARKED_ROLLBACK ||
                    status == Status.STATUS_PREPARED ||
                    status == Status.STATUS_PREPARING ||
                    status == Status.STATUS_ROLLING_BACK;
        } catch (SystemException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public javax.transaction.UserTransaction getUserTransaction() {
        return UserTransaction.userTransaction();
    }
}
