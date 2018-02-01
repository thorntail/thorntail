package org.jboss.unimbus.datasources;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

import org.jboss.jca.core.connectionmanager.ConnectionManagerFactory;

/**
 * Created by bob on 1/31/18.
 */
@ApplicationScoped
public class ConnectionManagerFactoryProducer {

    @Produces
    ConnectionManagerFactory connectionManagerFactory() {
        return new ConnectionManagerFactory();
    }

}
