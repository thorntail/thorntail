package org.jboss.unimbus.datasources;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.resource.spi.ConnectionManager;
import javax.resource.spi.TransactionSupport;
import javax.security.auth.Subject;

import org.jboss.jca.common.api.metadata.common.FlushStrategy;
import org.jboss.jca.core.api.connectionmanager.ccm.CachedConnectionManager;
import org.jboss.jca.core.api.management.ManagedEnlistmentTrace;
import org.jboss.jca.core.connectionmanager.ConnectionManagerFactory;
import org.jboss.jca.core.connectionmanager.TxConnectionManager;
import org.jboss.jca.core.connectionmanager.pool.api.Pool;
import org.jboss.jca.core.spi.security.SubjectFactory;
import org.jboss.jca.core.spi.transaction.TransactionIntegration;

/**
 * Created by bob on 1/31/18.
 */
@ApplicationScoped
public class ConnectionManagerRegistry {

    @PostConstruct
    void init() {
        this.poolRegistry.getPools().entrySet()
                .forEach( e->{
                    init(e.getKey(), e.getValue());
                });
    }

    void init(String id, Pool pool) {
        TxConnectionManager connectionManager = factory.createTransactional(
                TransactionSupport.TransactionSupportLevel.LocalTransaction,
                pool,
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

        register(id, connectionManager);
    }

    public void register(String id, ConnectionManager connectionManager) {
        this.connectionManagers.put( id, connectionManager);
    }

    public ConnectionManager get(String id) {
        return this.connectionManagers.get(id);
    }

    @Inject
    ConnectionManagerFactory factory;

    @Inject
    private PoolRegistry poolRegistry;

    private SubjectFactory subjectFactory;

    private String securityDomain;

    private boolean useCcm;

    private CachedConnectionManager cachedConnectionManager;

    private boolean shareable;

    private boolean enlistment;

    private boolean connectable;

    private Boolean tracking;

    private ManagedEnlistmentTrace managedEnlistmentTrace;

    private FlushStrategy flushStrategy = FlushStrategy.FAILING_CONNECTION_ONLY;

    private Integer allocationRetry;

    private Long allocationRetryWaitMillis;

    @Inject
    private TransactionIntegration txIntegration;

    private Boolean interleaving;

    private Integer xaResourceTimeout;

    private Boolean isSameRMOverride;

    private Boolean wrapXAResource;

    private Boolean padXid;

    private Map<String, ConnectionManager> connectionManagers = new HashMap<>();
}
