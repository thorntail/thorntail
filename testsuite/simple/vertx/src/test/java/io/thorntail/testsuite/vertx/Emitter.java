package io.thorntail.testsuite.vertx;

import static io.thorntail.testsuite.vertx.VertxInitializerTest.BUNNY;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;

import io.thorntail.vertx.VertxPublish;
import io.vertx.core.Vertx;

@ApplicationScoped
public class Emitter {

    @Inject
    Vertx vertx;

    @VertxPublish(BUNNY)
    @Inject
    Event<String> bunnyEvent;

    void emit(String address, String payload) {
        vertx.eventBus().send(address, payload);
    }

    void emitBunny(String payload) {
        bunnyEvent.fire(payload);
    }

}
