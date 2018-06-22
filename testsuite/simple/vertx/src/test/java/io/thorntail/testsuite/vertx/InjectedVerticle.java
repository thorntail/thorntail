package io.thorntail.testsuite.vertx;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;

@ApplicationScoped
public class InjectedVerticle extends AbstractVerticle {

    @Inject
    Results results;

    @Override
    public void start(Future<Void> startFuture) throws Exception {
        vertx.eventBus().consumer(VertxInitializerTest.BUNNY, (message) -> results.add(InjectedVerticle.class.getSimpleName() + message.body()))
                .completionHandler(r -> {
                    if (r.succeeded()) {
                        startFuture.complete();
                    } else {
                        startFuture.fail(r.cause());
                    }
                });
    }

}
