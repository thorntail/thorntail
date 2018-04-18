package io.thorntail.jms;

import javax.jms.Destination;
import javax.jms.JMSConsumer;
import javax.jms.JMSContext;
import javax.jms.JMSRuntimeException;
import javax.jms.Message;
import javax.jms.MessageListener;

/**
 * Created by bob on 2/21/18.
 */
public class SimpleWrappedJMSConsumer implements JMSConsumer {

    protected SimpleWrappedJMSConsumer(JMSContext context, Destination destination, JMSConsumer delegate) {
        this.context = context;
        this.destination = destination;
        this.delegate = delegate;
    }

    protected JMSContext getContext() {
        return this.context;
    }

    protected Destination getDestination() {
        return this.destination;
    }

    protected JMSConsumer getDelegate() {
        return delegate;
    }

    @Override
    public String getMessageSelector() {
        return delegate.getMessageSelector();
    }

    @Override
    public MessageListener getMessageListener() throws JMSRuntimeException {
        return delegate.getMessageListener();
    }

    @Override
    public void setMessageListener(MessageListener listener) throws JMSRuntimeException {
        delegate.setMessageListener(listener);
    }

    @Override
    public Message receive() {
        return delegate.receive();
    }

    @Override
    public Message receive(long timeout) {
        return delegate.receive(timeout);
    }

    @Override
    public Message receiveNoWait() {
        return delegate.receiveNoWait();
    }

    @Override
    public void close() {
        delegate.close();
    }

    @Override
    public <T> T receiveBody(Class<T> c) {
        return delegate.receiveBody(c);
    }

    @Override
    public <T> T receiveBody(Class<T> c, long timeout) {
        return delegate.receiveBody(c, timeout);
    }

    @Override
    public <T> T receiveBodyNoWait(Class<T> c) {
        return delegate.receiveBodyNoWait(c);
    }

    private final JMSContext context;
    private final Destination destination;
    private final JMSConsumer delegate;
}
