package org.jboss.unimbus.vertx.impl.opentracing;

import javax.annotation.Priority;
import javax.enterprise.context.ApplicationScoped;
import javax.interceptor.InvocationContext;

import io.opentracing.ActiveSpan;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import io.vertx.core.eventbus.Message;
import io.vertx.resourceadapter.inflow.VertxListener;
import org.jboss.unimbus.tracing.OneArgSpanHandler;

/**
 * Created by bob on 3/2/18.
 */
@ApplicationScoped
@Priority(0)
public class VertxListenerSpanHandler extends OneArgSpanHandler<Message<?>> {

    @Override
    public boolean canHandle(InvocationContext ctx) {
        return VertxListener.class.isAssignableFrom(ctx.getTarget().getClass());
    }

    public ActiveSpan handle(Message message) {
        SpanContext parent = TraceUtils.extract(message);
        Tracer.SpanBuilder builder = TraceUtils.build("vertx-receive", message);
        if (parent != null) {
            builder.asChildOf(parent);
        }
        return builder.startActive();
    }

}
