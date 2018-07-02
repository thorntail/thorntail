package io.thorntail.vertx.web.tracing;

import javax.annotation.Priority;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.interceptor.InvocationContext;

import io.opentracing.ActiveSpan;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import io.opentracing.Tracer.SpanBuilder;
import io.opentracing.propagation.Format;
import io.opentracing.tag.Tags;
import io.thorntail.tracing.SpanHandler;
import io.thorntail.vertx.web.RouteExtension;
import io.thorntail.vertx.web.VertxWebLogger;
import io.thorntail.vertx.web.WebRoute;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.RoutingContext;

/**
 * Starts span for traced {@link WebRoute} handlers and observers.
 *
 * @author Martin Kouba
 */
@Priority(1)
@ApplicationScoped
public class WebRouteSpanHandler extends SpanHandler {

    @Inject
    private RouteExtension routeExtension;

    @Inject
    private Tracer tracer;

    @Override
    public boolean canHandle(InvocationContext ctx) {
        return (ctx.getTarget() instanceof Handler && "handle".equals(ctx.getMethod().getName()) && routeExtension.isHandlerType(ctx.getTarget().getClass()))
                || routeExtension.isRouteObserver(ctx.getMethod());
    }

    @Override
    public ActiveSpan handle(InvocationContext ctx) {
        RoutingContext routingContext = null;
        for (Object param : ctx.getParameters()) {
            if (param instanceof RoutingContext) {
                routingContext = (RoutingContext) param;
                break;
            }
        }
        if (routingContext == null) {
            return null;
        }
        VertxWebLogger.LOG.startSpanForWebRoute(ctx.getMethod());
        HttpMethod httpMethod = routingContext.request().method();
        SpanContext parent = tracer.extract(Format.Builtin.HTTP_HEADERS, new RoutingContextTextMap(routingContext));
        SpanBuilder builder = tracer.buildSpan(String.format("%s:%s.%s", httpMethod, ctx.getMethod().getDeclaringClass().getName(), ctx.getMethod().getName()));
        builder.withTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_SERVER);
        builder.withTag(Tags.HTTP_METHOD.getKey(), httpMethod.toString());
        builder.withTag(Tags.HTTP_URL.getKey(), routingContext.request().absoluteURI());
        if (parent != null) {
            builder.asChildOf(parent);
        }
        return builder.startActive();
    }

}
