package io.thorntail.testsuite.vertx.web;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;

import io.thorntail.vertx.web.WebRoute;
import io.vertx.ext.web.RoutingContext;

@ApplicationScoped
public class HelloObserver {

    @WebRoute(path = "/helloObserver")
    void helloObserver(@Observes RoutingContext ctx, HelloService service) {
        ctx.response().setStatusCode(200).end(service.sayHello() + ":observer");
    }

}
