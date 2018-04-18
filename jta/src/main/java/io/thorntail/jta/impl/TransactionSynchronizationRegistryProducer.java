package io.thorntail.jta.impl;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.transaction.TransactionSynchronizationRegistry;

import com.arjuna.ats.internal.jta.transaction.arjunacore.TransactionSynchronizationRegistryImple;

/**
 * Created by bob on 2/9/18.
 */
@ApplicationScoped
public class TransactionSynchronizationRegistryProducer {

    @Produces
    @ApplicationScoped
    TransactionSynchronizationRegistry transactionSynchronizationRegistry() {
        return new TransactionSynchronizationRegistryImple();
    }
}
