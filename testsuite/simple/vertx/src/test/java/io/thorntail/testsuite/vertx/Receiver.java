package io.thorntail.testsuite.vertx;

import static io.thorntail.testsuite.vertx.VertxInitializerTest.BUNNY;
import static io.thorntail.testsuite.vertx.VertxInitializerTest.DUMMY;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import io.thorntail.vertx.VertxConsume;
import io.thorntail.vertx.VertxMessage;

@ApplicationScoped
public class Receiver {

    @Inject
    Results results;

    void onDummy(@Observes @VertxConsume(DUMMY) VertxMessage message) {
        results.add(message.address() + message.body());
    }

    void onBunny(@Observes @VertxConsume(BUNNY) VertxMessage message) {
        results.add(message.address() + message.body());
    }

}
