package io.thorntail.jms.impl.opentracing;

import io.opentracing.Scope;
import javax.annotation.Priority;
import javax.enterprise.context.ApplicationScoped;
import javax.interceptor.InvocationContext;
import javax.jms.Message;
import javax.jms.MessageListener;

import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import io.opentracing.tag.Tags;
import io.thorntail.tracing.OneArgSpanHandler;

/**
 * Created by bob on 3/2/18.
 */
@ApplicationScoped
@Priority(0)
public class MessageListenerSpanHandler extends OneArgSpanHandler<Message> {

    @Override
    public boolean canHandle(InvocationContext ctx) {
        return MessageListener.class.isAssignableFrom(ctx.getTarget().getClass());
    }

    public Scope handle(Message message) {
        SpanContext parent = TraceUtils.extract(message);
        Tracer.SpanBuilder builder = TraceUtils.build("jms-receive", message);
        if (parent != null) {
            builder.asChildOf(parent);
        }
        builder.withTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_CONSUMER);
        return builder.startActive(true);
    }

}
