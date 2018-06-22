package io.thorntail.testsuite.vertx.web;

import javax.enterprise.context.Dependent;
import javax.enterprise.event.Observes;

import io.vertx.ext.web.Router;

@Dependent
public class NextHandlerRegistrar {

    void registerNext(@Observes Router router) {
        router.get("/next").handler(ctx -> ctx.response().setStatusCode(200).end("nextOne"));
    }

}
