package org.jboss.unimbus.jms.impl.opentracing;

import javax.jms.Destination;
import javax.jms.JMSConsumer;
import javax.jms.JMSException;
import javax.jms.JMSRuntimeException;
import javax.jms.Message;

import io.opentracing.References;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import io.opentracing.tag.Tags;
import org.jboss.unimbus.jms.SimpleWrappedJMSConsumer;

/**
 * Created by bob on 2/21/18.
 */
public class TracedJMSConsumer extends SimpleWrappedJMSConsumer {

    protected TracedJMSConsumer(TracedJMSContext context, Destination destination, JMSConsumer delegate) {
        super(context, destination, delegate);
    }

    protected TracedJMSContext getContext() {
        return (TracedJMSContext) super.getContext();
    }

    protected Message trace(Message message) {
        if (message == null) {
            return message;
        }
        SpanContext parent = TraceUtils.extract(message);
        Tracer.SpanBuilder builder = TraceUtils.build("jms-receive", message);
        if (builder != null) {
            if (parent != null) {
                builder.asChildOf(parent);
                //builder.addReference(References.FOLLOWS_FROM, parent);
            }
            builder.withTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_CONSUMER);
            builder.startActive().deactivate();
        }
        return message;
    }

    public Message receive() {
        return trace(getDelegate().receive());
    }

    public Message receive(long timeout) {
        return trace(getDelegate().receive(timeout));
    }

    public Message receiveNoWait() {
        return trace(getDelegate().receiveNoWait());
    }

    public <T> T receiveBody(Class<T> c) {
        try {
            Message message = receive();
            return message == null ? null : message.getBody(c);
        } catch (JMSException e) {
            throw new JMSRuntimeException(e.getMessage(), e.getErrorCode(), e);
        }
    }

    public <T> T receiveBody(Class<T> c, long timeout) {
        try {
            Message message = receive(timeout);
            getContext().setLastMessageReceived(message);
            return message == null ? null : message.getBody(c);
        } catch (JMSException e) {
            throw new JMSRuntimeException(e.getMessage(), e.getErrorCode(), e);
        }
    }

    public <T> T receiveBodyNoWait(Class<T> c) {
        try {
            Message message = receiveNoWait();
            getContext().setLastMessageReceived(message);
            return message == null ? null : message.getBody(c);
        } catch (JMSException e) {
            throw new JMSRuntimeException(e.getMessage(), e.getErrorCode(), e);
        }
    }
}
