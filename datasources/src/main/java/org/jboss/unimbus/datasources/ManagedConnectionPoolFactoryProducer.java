package org.jboss.unimbus.datasources;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

import org.jboss.jca.core.connectionmanager.pool.mcp.ManagedConnectionPool;
import org.jboss.jca.core.connectionmanager.pool.mcp.ManagedConnectionPoolFactory;

/**
 * Created by bob on 1/31/18.
 */
@ApplicationScoped
public class ManagedConnectionPoolFactoryProducer {

    @Produces
    @ApplicationScoped
    ManagedConnectionPoolFactory managedConnectionPool() {
        return new ManagedConnectionPoolFactory();
    }
}
