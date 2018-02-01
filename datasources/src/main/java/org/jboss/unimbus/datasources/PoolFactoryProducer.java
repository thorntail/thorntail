package org.jboss.unimbus.datasources;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

import org.jboss.jca.core.connectionmanager.pool.api.PoolFactory;

/**
 * Created by bob on 1/31/18.
 */
@ApplicationScoped
public class PoolFactoryProducer {

    @Produces
    PoolFactory poolFactory() {
        return new PoolFactory();
    }
}
