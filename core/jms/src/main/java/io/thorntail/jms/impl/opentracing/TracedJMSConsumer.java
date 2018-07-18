package io.thorntail.jms.impl.opentracing;

import io.opentracing.References;
import javax.jms.Destination;
import javax.jms.JMSConsumer;
import javax.jms.JMSException;
import javax.jms.JMSRuntimeException;
import javax.jms.Message;

import io.opentracing.SpanContext;
import io.opentracing.tag.Tags;
import io.thorntail.jms.SimpleWrappedJMSConsumer;

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
        TraceUtils.build("jms-receive", message)
            .addReference(References.FOLLOWS_FROM, parent)
            .asChildOf(parent)
            .withTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_CONSUMER)
            .start().finish();
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
