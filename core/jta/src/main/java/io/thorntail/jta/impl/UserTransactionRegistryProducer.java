package io.thorntail.jta.impl;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

import org.jboss.tm.usertx.UserTransactionRegistry;

/**
 * Created by bob on 2/9/18.
 */
@ApplicationScoped
public class UserTransactionRegistryProducer {

    @Produces
    @ApplicationScoped
    UserTransactionRegistry userTransactionRegistry() {
        return new UserTransactionRegistry();
    }
}
