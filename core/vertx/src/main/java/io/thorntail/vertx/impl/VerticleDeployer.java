package io.thorntail.vertx.impl;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import io.thorntail.events.LifecycleEvent;
import io.vertx.core.Verticle;
import io.vertx.core.Vertx;

/**
 * Created by bob on 6/21/18.
 */
@ApplicationScoped
public class VerticleDeployer {

    void deploy(@Observes LifecycleEvent.Deploy event) {
        if ( this.verticles.isUnsatisfied() ) {
            return;
        }

        Vertx vertx = this.vertxInstance.get();

        this.verticles.forEach( v->{
            vertx.deployVerticle(v);
            VertxMessages.MESSAGES.deployedVerticle(v.getClass().getName());
        });
    }

    @Inject
    @Any
    Instance<Verticle> verticles;

    @Inject
    Instance<Vertx> vertxInstance;
}
