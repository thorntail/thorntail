package org.jboss.unimbus.undertow;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

import io.undertow.Handlers;
import io.undertow.server.handlers.PathHandler;

/**
 * Created by bob on 1/15/18.
 */
@ApplicationScoped
public class RootHandlerProducer {

    @Produces
    @ApplicationScoped
    PathHandler handler() {
        //return Handlers.path(Handlers.redirect("/"));
        return new PathHandler();
    }

}
