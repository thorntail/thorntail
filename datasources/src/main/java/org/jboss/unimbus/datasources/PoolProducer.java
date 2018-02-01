package org.jboss.unimbus.datasources;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.resource.spi.ManagedConnectionFactory;

import org.jboss.jca.core.api.connectionmanager.pool.PoolConfiguration;
import org.jboss.jca.core.connectionmanager.pool.api.Pool;
import org.jboss.jca.core.connectionmanager.pool.api.PoolFactory;
import org.jboss.jca.core.connectionmanager.pool.api.PoolStrategy;
import org.jboss.jca.core.connectionmanager.pool.mcp.SemaphoreArrayListManagedConnectionPool;

/**
 * Created by bob on 1/31/18.
 */
@ApplicationScoped
public class PoolProducer {

    @Produces
    Pool pool() {
        return poolFactory.create(
                this.strategy,
                this.managedConnectionFactory,
                this.poolConfiguration,
                this.noTxSeparatePools,
                this.shareable,
                this.managedConnectionPool
        );
    }

    @Inject
    PoolFactory poolFactory;

    private PoolStrategy strategy = PoolStrategy.ONE_POOL;

    @Inject
    private ManagedConnectionFactory managedConnectionFactory;

    private PoolConfiguration poolConfiguration = new PoolConfiguration();

    private boolean noTxSeparatePools;

    private boolean shareable;

    private String managedConnectionPool = SemaphoreArrayListManagedConnectionPool.class.getName();
}
