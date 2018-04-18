package io.thorntail.jta.impl;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

import com.arjuna.ats.jta.UserTransaction;

/**
 * Created by bob on 1/16/18.
 */
@ApplicationScoped
public class UserTransactionProducer {

    @Produces
    @ApplicationScoped
    javax.transaction.UserTransaction userTransaction() {
        return UserTransaction.userTransaction();
    }


}
