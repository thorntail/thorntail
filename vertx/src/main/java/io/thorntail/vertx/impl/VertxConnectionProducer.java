package io.thorntail.vertx.impl;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.resource.ResourceException;

import io.vertx.resourceadapter.VertxConnection;
import io.vertx.resourceadapter.VertxConnectionFactory;

/**
 * Created by bob on 2/12/18.
 */
@ApplicationScoped
public class VertxConnectionProducer {

    @Produces
    VertxConnection vertxConnection() throws ResourceException {
        return connectionFactory.getVertxConnection();
    }

    @Inject
    VertxConnectionFactory connectionFactory;


}
