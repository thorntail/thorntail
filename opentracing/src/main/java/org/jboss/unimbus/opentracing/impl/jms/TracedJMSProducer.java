package org.jboss.unimbus.opentracing.impl.jms;

import javax.jms.Destination;
import javax.jms.JMSProducer;
import javax.jms.Message;

import io.opentracing.ActiveSpan;
import io.opentracing.Tracer;
import org.jboss.unimbus.jms.SimpleWrappedJMSProducer;

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
        Tracer.SpanBuilder builder = TraceUtils.build("send", message, destination);
        ActiveSpan sendSpan = null;
        if (builder != null) {
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
