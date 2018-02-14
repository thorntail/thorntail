package org.jboss.unimbus.vertx;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.resource.ResourceException;

import io.vertx.resourceadapter.VertxConnection;
import io.vertx.resourceadapter.VertxEventBus;

/**
 * Created by bob on 2/12/18.
 */
@Dependent
public class EventBusProducer {

    @Produces
    VertxEventBus eventBus() throws ResourceException {
        return this.connection.vertxEventBus();
    }

    @Inject
    VertxConnection connection;
}
