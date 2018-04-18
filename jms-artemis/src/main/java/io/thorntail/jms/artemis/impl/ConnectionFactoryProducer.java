package io.thorntail.jms.artemis.impl;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.jms.ConnectionFactory;
import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 * Created by bob on 2/12/18.
 */
@ApplicationScoped
public class ConnectionFactoryProducer {

    @PostConstruct
    void construct() {
        try {
            this.connectionFactory = (ConnectionFactory) this.context.lookup("java:jboss/artemis/connection-factory");
        } catch (NamingException e) {
            throw new RuntimeException(e);
        }
    }

    @Produces
    ConnectionFactory connectionFactory() {
        return this.connectionFactory;
    }

    @Inject
    private InitialContext context;

    private ConnectionFactory connectionFactory;
}
