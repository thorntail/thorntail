package io.thorntail.testsuite.vertx;

import javax.inject.Inject;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;

/**
 * Created by bob on 6/21/18.
 */
public class VerticleA extends AbstractVerticle implements Handler<Message<JsonObject>> {
    @Override
    public void start() throws Exception {
        getVertx().eventBus().consumer( "verticle.a", this);
    }

    @Override
    public void handle(Message<JsonObject> event) {
        event.reply( this.prefix.getPrefix() + ": " + event.body());
    }

    @Inject
    Prefix prefix;
}
