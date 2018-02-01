package org.jboss.unimbus.datasources;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.Vetoed;
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
public class PoolRegistry {

    @PostConstruct
    void init() {
        this.managedConnectionFactoryRegistry.getFactories().entrySet()
                .forEach(e -> {
                    init(e.getKey(), e.getValue());
                });
    }

    void init(String id, ManagedConnectionFactory factory) {
        Pool pool = poolFactory.create(
                this.strategy,
                factory,
                this.poolConfiguration,
                this.noTxSeparatePools,
                this.shareable,
                this.managedConnectionPool
        );

        register( id, pool );
    }

    public void register(String id, Pool pool) {
        this.pools.put( id, pool );
    }

    public Map<String,Pool> getPools() {
        return Collections.unmodifiableMap(this.pools);
    }

    @Inject
    ManagedConnectionFactoryRegistry managedConnectionFactoryRegistry;

    @Inject
    PoolFactory poolFactory;

    private PoolStrategy strategy = PoolStrategy.ONE_POOL;

    private PoolConfiguration poolConfiguration = new PoolConfiguration();

    private boolean noTxSeparatePools;

    private boolean shareable;

    private String managedConnectionPool = SemaphoreArrayListManagedConnectionPool.class.getName();

    private Map<String, Pool> pools = new HashMap<>();
}
