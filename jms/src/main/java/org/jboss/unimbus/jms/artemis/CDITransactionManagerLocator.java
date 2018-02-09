package org.jboss.unimbus.jms.artemis;

import javax.enterprise.inject.spi.Unmanaged;
import javax.inject.Inject;
import javax.transaction.TransactionManager;

import org.apache.activemq.artemis.service.extensions.transactions.TransactionManagerLocator;

/**
 * Created by bob on 2/9/18.
 */
public class CDITransactionManagerLocator implements TransactionManagerLocator {

    @Override
    public TransactionManager getTransactionManager() {
        Unmanaged.UnmanagedInstance<CDITransactionManagerLocator> unmanaged = new Unmanaged<>(CDITransactionManagerLocator.class).newInstance()
                .produce()
                .inject()
                .postConstruct();

        try {
            return unmanaged.get().tm;
        } finally {
            unmanaged.preDestroy().dispose();
        }
    }

    @Inject
    TransactionManager tm;
}
