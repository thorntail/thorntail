package io.thorntail.vertx.impl;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import io.vertx.resourceadapter.VertxConnectionFactory;

/**
 * Created by bob on 2/12/18.
 */
@ApplicationScoped
public class VertxConnectionFactoryProducer {

    @Produces
    VertxConnectionFactory vertxConnectionFactory() throws NamingException {
        return (VertxConnectionFactory) this.context.lookup("java:jboss/vertx/connection-factory");
    }

    @Inject
    InitialContext context;
}
