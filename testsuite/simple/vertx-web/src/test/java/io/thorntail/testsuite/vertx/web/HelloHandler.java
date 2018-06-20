package io.thorntail.testsuite.vertx.web;

import static io.thorntail.vertx.web.WebRoute.HandlerType.BLOCKING;

import javax.inject.Inject;

import io.thorntail.vertx.web.WebRoute;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.RoutingContext;

@WebRoute("/hello")
@WebRoute(value = "/helloBlocking", methods= HttpMethod.GET, type = BLOCKING)
public class HelloHandler implements Handler<RoutingContext> {

    @Inject
    HelloService service;

    @Override
    public void handle(RoutingContext ctx) {
        ctx.response().setStatusCode(200).end(service.sayHello());
    }

}
