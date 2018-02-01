package org.jboss.unimbus.datasources;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.Vetoed;
import javax.inject.Inject;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.ManagedConnectionFactory;
import javax.security.auth.Subject;

import org.jboss.jca.core.api.connectionmanager.pool.PoolConfiguration;
import org.jboss.jca.core.connectionmanager.ConnectionManager;
import org.jboss.jca.core.connectionmanager.pool.api.Pool;
import org.jboss.jca.core.connectionmanager.pool.mcp.ManagedConnectionPool;
import org.jboss.jca.core.connectionmanager.pool.mcp.ManagedConnectionPoolFactory;

/**
 * Created by bob on 1/31/18.
 */
//@ApplicationScoped
    @Vetoed
public class ManagedConnectionPoolProducer {

    @Produces
    @ApplicationScoped
    ManagedConnectionPool managedConnectionPool() throws Throwable {
        return this.poolFactory.create(
                this.connectionFactory,
                this.connectionManager,
                this.subject,
                this.connectionRequestInfo,
                this.poolConfiguration,
                this.pool);
    }

    @Inject
    ManagedConnectionPoolFactory poolFactory;

    @Inject
    ManagedConnectionFactory connectionFactory;

    @Inject
    ConnectionManager connectionManager;

    @Inject
    Subject subject;

    @Inject
    ConnectionRequestInfo connectionRequestInfo;

    @Inject
    PoolConfiguration poolConfiguration;

    @Inject
    Pool pool;
}
