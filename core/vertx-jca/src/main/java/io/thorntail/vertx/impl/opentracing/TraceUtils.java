package io.thorntail.vertx.impl.opentracing;

import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import io.opentracing.propagation.Format;
import io.opentracing.tag.Tags;
import io.opentracing.util.GlobalTracer;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;

/**
 * Created by bob on 2/21/18.
 */
class TraceUtils {

    static SpanContext extract(Message<?> message) {
        Tracer tracer = GlobalTracer.get();
        if (tracer == null) {
            return null;
        }
        MessageAdapter carrier = new MessageAdapter(message);
        return tracer.extract(Format.Builtin.HTTP_HEADERS, carrier);
    }

    static void inject(Message<?> message) {
        Tracer tracer = GlobalTracer.get();
        if ( tracer == null ) {
            return;
        }

        MessageAdapter carrier = new MessageAdapter(message);
        SpanContext context = tracer.activeSpan().context();
        tracer.inject( context, Format.Builtin.HTTP_HEADERS, carrier);
    }

    static void inject(DeliveryOptions options) {
        Tracer tracer = GlobalTracer.get();
        if ( tracer == null ) {
            return;
        }

        DeliveryOptionsAdapter carrier = new DeliveryOptionsAdapter(options);
        Span span = tracer.activeSpan();
        if ( span != null ) {
            SpanContext context = span.context();
            tracer.inject(context, Format.Builtin.HTTP_HEADERS, carrier);
        }
    }

    static Tracer.SpanBuilder build(String operationName, Message message) {
        return build(operationName, message, null);
    }

    static Tracer.SpanBuilder build(String operationName, Message message, String address) {
        Tracer tracer = GlobalTracer.get();
        if (tracer == null) {
            return null;
        }

        Tracer.SpanBuilder builder = tracer.buildSpan(operationName);

        if (address == null) {
            address = message.address();
        }
        if (address != null) {
            builder.withTag(Tags.MESSAGE_BUS_DESTINATION.getKey(), address );
        }

        builder.withTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_CONSUMER);

        return builder;
    }
}
