package io.thorntail.vertx.tracing;

import io.opentracing.Scope;
import javax.annotation.Priority;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.interceptor.InvocationContext;

import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import io.opentracing.Tracer.SpanBuilder;
import io.opentracing.propagation.Format;
import io.opentracing.tag.Tags;
import io.thorntail.tracing.SpanHandler;
import io.thorntail.vertx.VertxExtension;
import io.thorntail.vertx.VertxLogger;
import io.thorntail.vertx.VertxMessage;

/**
 * Starts span for traced {@link VertxMessage} observers.
 *
 * @author Martin Kouba
 */
@Priority(1)
@ApplicationScoped
public class VertxMessageSpanHandler extends SpanHandler {

    static final String OP_NAME_PREFIX = "vertx-observer-notify";

    @Inject
    private VertxExtension vertExtension;

    @Inject
    private Tracer tracer;

    @Override
    public boolean canHandle(InvocationContext ctx) {
        return vertExtension.isVertxMessageObserver(ctx.getMethod());
    }

    @Override
    public Scope handle(InvocationContext ctx) {
        VertxMessage vertxMessage = null;
        for (Object param : ctx.getParameters()) {
            if (param instanceof VertxMessage) {
                vertxMessage = (VertxMessage) param;
                break;
            }
        }
        if (vertxMessage == null) {
            return null;
        }
        VertxLogger.LOG.startSpanForVertxMessageObserver(ctx.getMethod());
        SpanContext parent = tracer.extract(Format.Builtin.HTTP_HEADERS, new VertxMessageTextMap(vertxMessage));
        SpanBuilder builder = tracer.buildSpan(String.format("%s:%s.%s", OP_NAME_PREFIX, ctx.getMethod().getDeclaringClass().getName(), ctx.getMethod().getName()));
        builder.withTag(Tags.MESSAGE_BUS_DESTINATION.getKey(), vertxMessage.address());
        builder.withTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_CONSUMER);
        if (parent != null) {
            builder.asChildOf(parent);
        }
        return builder.startActive(true);
    }

}
