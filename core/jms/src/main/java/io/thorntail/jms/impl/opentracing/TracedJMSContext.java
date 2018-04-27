package io.thorntail.jms.impl.opentracing;

import javax.jms.Destination;
import javax.jms.JMSConsumer;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.JMSProducer;
import javax.jms.JMSRuntimeException;
import javax.jms.Message;

import io.thorntail.jms.SimpleWrappedJMSContext;

/**
 * Created by bob on 2/21/18.
 */
public class TracedJMSContext extends SimpleWrappedJMSContext {

    protected TracedJMSContext(JMSContext delegate) {
        super(delegate);
    }

    public JMSProducer createProducer() {
        return new TracedJMSProducer(this, getDelegate().createProducer());
    }

    public JMSConsumer createConsumer(Destination destination) {
        return new TracedJMSConsumer(this, destination, getDelegate().createConsumer(destination));
    }

    public JMSConsumer createConsumer(Destination destination, String messageSelector) {
        return new TracedJMSConsumer(this, destination, getDelegate().createConsumer(destination, messageSelector));
    }

    public JMSConsumer createConsumer(Destination destination, String messageSelector, boolean noLocal) {
        return new TracedJMSConsumer(this, destination, getDelegate().createConsumer(destination, messageSelector, noLocal));
    }

    void setLastMessageReceived(Message message) {
        if (getSessionMode() == JMSContext.CLIENT_ACKNOWLEDGE) {
            this.lastMessageReceived = message;
        }
    }

    @Override
    public void acknowledge() {
        if (this.lastMessageReceived != null) {
            try {
                this.lastMessageReceived.acknowledge();
            } catch (JMSException e) {
                throw new JMSRuntimeException(e.getMessage(), e.getErrorCode(), e);
            }
        }
        super.acknowledge();
    }

    private Message lastMessageReceived;
}

