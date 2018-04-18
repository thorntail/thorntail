package io.thorntail.jca.impl.ironjacamar;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import org.jboss.jca.core.api.connectionmanager.ccm.CachedConnectionManager;
import org.jboss.jca.core.connectionmanager.ccm.CachedConnectionManagerImpl;
import org.jboss.jca.core.spi.transaction.TransactionIntegration;

/**
 * Created by bob on 2/9/18.
 */
@ApplicationScoped
public class CachedConnectionManagerProducer {

    @Produces
    @ApplicationScoped
    CachedConnectionManager cachedConnectionManager() {
        return new CachedConnectionManagerImpl(this.transactionIntegration);
    }

    @Inject
    TransactionIntegration transactionIntegration;
}
