package io.thorntail.vertx.tracing;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import io.opentracing.ActiveSpan;
import io.opentracing.Tracer;
import io.opentracing.Tracer.SpanBuilder;
import io.opentracing.propagation.Format;
import io.opentracing.tag.Tags;
import io.thorntail.TraceMode;
import io.thorntail.vertx.VertxInitializer;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.SendContext;

/**
 * Intercepts {@link EventBus} send operations.
 *
 * @author Martin Kouba
 * @see EventBus#addInterceptor(Handler)
 */
@SuppressWarnings("rawtypes")
@Dependent
public class VertxEventBusInterceptor implements Handler<SendContext> {

    public static final String OP_NAME_SEND = "vertx-send";

    @Inject
    private Tracer tracer;

    @Inject
    private VertxInitializer vertxInitializer;

    @Override
    public void handle(SendContext sendContext) {
        ActiveSpan parent = tracer.activeSpan();
        if (TraceMode.ACTIVE.equals(vertxInitializer.getTraceMode()) && parent == null) {
            sendContext.next();
        } else {
            SpanBuilder spanBuilder = tracer.buildSpan(OP_NAME_SEND);
            if (parent != null) {
                spanBuilder.asChildOf(parent);
            }
            spanBuilder.withTag(Tags.MESSAGE_BUS_DESTINATION.getKey(), sendContext.message().address());
            spanBuilder.withTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_PRODUCER);
            try (ActiveSpan activeSpan = spanBuilder.startActive()) {
                tracer.inject(activeSpan.context(), Format.Builtin.HTTP_HEADERS, new HeadersTextMap(sendContext.message().headers()));
                sendContext.next();
            }
        }
    }

}
