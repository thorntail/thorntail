package org.jboss.unimbus.opentracing.impl.vertx;

import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import io.opentracing.propagation.Format;
import io.opentracing.util.GlobalTracer;
import io.vertx.core.eventbus.Message;

/**
 * Created by bob on 2/21/18.
 */
class TraceUtils {

    static final String VERTX_ADDRESS_TAG = "vertx.address";

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
            builder.withTag(VERTX_ADDRESS_TAG, address );
        }

        return builder;
    }
}
