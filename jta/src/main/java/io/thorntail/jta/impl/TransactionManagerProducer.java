package io.thorntail.jta.impl;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

import com.arjuna.ats.jta.TransactionManager;


/**
 * Created by bob on 1/16/18.
 */
@ApplicationScoped
public class TransactionManagerProducer {

    @Produces
    @ApplicationScoped
    javax.transaction.TransactionManager transactionManager() {
        return TransactionManager.transactionManager();
    }
}
