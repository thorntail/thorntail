package io.thorntail.jms.impl.opentracing;

import javax.jms.Destination;
import javax.jms.JMSProducer;
import javax.jms.Message;

import io.opentracing.ActiveSpan;
import io.opentracing.Tracer;
import io.opentracing.tag.Tags;
import io.thorntail.jms.SimpleWrappedJMSProducer;

/**
 * Created by bob on 2/21/18.
 */
public class TracedJMSProducer extends SimpleWrappedJMSProducer {

    public TracedJMSProducer(TracedJMSContext context, JMSProducer delegate) {
        super(context, delegate);
    }

    protected TracedJMSContext getContext() {
        return (TracedJMSContext) super.getContext();
    }

    public JMSProducer send(Destination destination, Message message) {
        Tracer.SpanBuilder builder = TraceUtils.build("jms-send", message, destination);
        ActiveSpan sendSpan = null;
        if (builder != null) {
            builder.withTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_PRODUCER);
            sendSpan = builder.startActive();
            TraceUtils.inject(message);
        }
        try {
            return getDelegate().send(destination, message);
        } finally {
            if (sendSpan != null) {
                sendSpan.close();
            }
        }
    }

}
