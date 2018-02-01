package org.jboss.unimbus.datasources;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.resource.spi.ConnectionManager;
import javax.resource.spi.TransactionSupport;
import javax.security.auth.Subject;

import org.jboss.jca.common.api.metadata.common.FlushStrategy;
import org.jboss.jca.core.api.connectionmanager.ccm.CachedConnectionManager;
import org.jboss.jca.core.api.management.ManagedEnlistmentTrace;
import org.jboss.jca.core.connectionmanager.ConnectionManagerFactory;
import org.jboss.jca.core.connectionmanager.pool.api.Pool;
import org.jboss.jca.core.connectionmanager.tx.TxConnectionManagerImpl;
import org.jboss.jca.core.spi.security.SubjectFactory;
import org.jboss.jca.core.spi.transaction.TransactionIntegration;

/**
 * Created by bob on 1/31/18.
 */
@ApplicationScoped
public class ConnectionManagerProducer {

    @Produces
    ConnectionManager connectionManager() {
        return factory.createTransactional(
                TransactionSupport.TransactionSupportLevel.LocalTransaction,
                this.pool,
                this.subjectFactory,
                this.securityDomain,
                this.useCcm,
                this.cachedConnectionManager,
                this.shareable,
                this.enlistment,
                this.connectable,
                this.tracking,
                this.managedEnlistmentTrace,
                this.flushStrategy,
                this.allocationRetry,
                this.allocationRetryWaitMillis,
                this.txIntegration,
                this.interleaving,
                this.xaResourceTimeout,
                this.isSameRMOverride,
                this.wrapXAResource,
                this.padXid);
    }

    @Inject
    ConnectionManagerFactory factory;

    @Inject
    private Pool pool;

    private SubjectFactory subjectFactory = new SubjectFactory() {
        @Override
        public Subject createSubject() {
            System.err.println( "Create subject" );
            return null;
        }

        @Override
        public Subject createSubject(String sd) {
            System.err.println( "Create subject: " + sd);
            return null;
        }
    };

    private String securityDomain;

    private boolean useCcm;

    private CachedConnectionManager cachedConnectionManager;

    private boolean shareable;

    private boolean enlistment;

    private boolean connectable;

    private Boolean tracking;

    private ManagedEnlistmentTrace managedEnlistmentTrace;

    private FlushStrategy flushStrategy = FlushStrategy.ALL_CONNECTIONS;

    private Integer allocationRetry;

    private Long allocationRetryWaitMillis;

    @Inject
    private TransactionIntegration txIntegration;

    private Boolean interleaving;

    private Integer xaResourceTimeout;

    private Boolean isSameRMOverride;

    private Boolean wrapXAResource;

    private Boolean padXid;
}
