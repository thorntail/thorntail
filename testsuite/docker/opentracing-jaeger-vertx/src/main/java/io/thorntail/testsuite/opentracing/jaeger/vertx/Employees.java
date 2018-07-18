package io.thorntail.testsuite.opentracing.jaeger.vertx;

import io.jaegertracing.Configuration;
import io.jaegertracing.internal.JaegerSpanContext;
import io.opentracing.Span;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.eclipse.microprofile.opentracing.Traced;


import io.opentracing.Tracer;
import io.thorntail.vertx.web.WebRoute;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

/**
 *
 * @author Martin Kouba
 */
@ApplicationScoped
public class Employees {

    @Inject
    Vertx vertx;

    @Inject
    Tracer tracer;

    @Traced
    @WebRoute(path = "/employees")
    void getEmployees(@Observes RoutingContext ctx) throws Exception {
        HttpServerResponse response = ctx.response();
        String traceId = currentTraceId();

        vertx.eventBus().<JsonArray> send("employees", "", reply -> {
            if (reply.succeeded()) {
                response.putHeader("Content-type", "application/json");
                response.setStatusCode(200).end(new JsonObject().put("traceId", traceId).put("data", reply.result().body()).encode());
            } else {
                response.setStatusCode(500).end(reply.cause().toString());
            }
        });
    }

    private String currentTraceId() {
        Span activeSpan = tracer.activeSpan();
        if (activeSpan == null) {
            throw new IllegalStateException("No active span found");
        }
        JaegerSpanContext ctx = (JaegerSpanContext) activeSpan.context();
        return Long.toHexString(ctx.getTraceId());
    }

}
