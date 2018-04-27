package io.thorntail.jms;

import java.io.Serializable;

import javax.jms.BytesMessage;
import javax.jms.ConnectionMetaData;
import javax.jms.Destination;
import javax.jms.ExceptionListener;
import javax.jms.JMSConsumer;
import javax.jms.JMSContext;
import javax.jms.JMSProducer;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.QueueBrowser;
import javax.jms.StreamMessage;
import javax.jms.TemporaryQueue;
import javax.jms.TemporaryTopic;
import javax.jms.TextMessage;
import javax.jms.Topic;

/**
 * Created by bob on 2/21/18.
 */
public class SimpleWrappedJMSContext implements WrappedJMSContext {

    protected SimpleWrappedJMSContext(JMSContext delegate) {
        this.delegate = delegate;
    }

    @Override
    public JMSContext getDelegate() {
        return this.delegate;
    }

    @Override
    public JMSContext createContext(int sessionMode) {
        return delegate.createContext(sessionMode);
    }

    @Override
    public JMSProducer createProducer() {
        return delegate.createProducer();
    }

    @Override
    public String getClientID() {
        return delegate.getClientID();
    }

    @Override
    public void setClientID(String clientID) {
        delegate.setClientID(clientID);
    }

    @Override
    public ConnectionMetaData getMetaData() {
        return delegate.getMetaData();
    }

    @Override
    public ExceptionListener getExceptionListener() {
        return delegate.getExceptionListener();
    }

    @Override
    public void setExceptionListener(ExceptionListener listener) {
        delegate.setExceptionListener(listener);
    }

    @Override
    public void start() {
        delegate.start();
    }

    @Override
    public void stop() {
        delegate.stop();
    }

    @Override
    public void setAutoStart(boolean autoStart) {
        delegate.setAutoStart(autoStart);
    }

    @Override
    public boolean getAutoStart() {
        return delegate.getAutoStart();
    }

    @Override
    public void close() {
        delegate.close();
    }

    @Override
    public BytesMessage createBytesMessage() {
        return delegate.createBytesMessage();
    }

    @Override
    public MapMessage createMapMessage() {
        return delegate.createMapMessage();
    }

    @Override
    public Message createMessage() {
        return delegate.createMessage();
    }

    @Override
    public ObjectMessage createObjectMessage() {
        return delegate.createObjectMessage();
    }

    @Override
    public ObjectMessage createObjectMessage(Serializable object) {
        return delegate.createObjectMessage(object);
    }

    @Override
    public StreamMessage createStreamMessage() {
        return delegate.createStreamMessage();
    }

    @Override
    public TextMessage createTextMessage() {
        return delegate.createTextMessage();
    }

    @Override
    public TextMessage createTextMessage(String text) {
        return delegate.createTextMessage(text);
    }

    @Override
    public boolean getTransacted() {
        return delegate.getTransacted();
    }

    @Override
    public int getSessionMode() {
        return delegate.getSessionMode();
    }

    @Override
    public void commit() {
        delegate.commit();
    }

    @Override
    public void rollback() {
        delegate.rollback();
    }

    @Override
    public void recover() {
        delegate.recover();
    }

    @Override
    public JMSConsumer createConsumer(Destination destination) {
        return delegate.createConsumer(destination);
    }

    @Override
    public JMSConsumer createConsumer(Destination destination, String messageSelector) {
        return delegate.createConsumer(destination, messageSelector);
    }

    @Override
    public JMSConsumer createConsumer(Destination destination, String messageSelector, boolean noLocal) {
        return delegate.createConsumer(destination, messageSelector, noLocal);
    }

    @Override
    public Queue createQueue(String queueName) {
        return delegate.createQueue(queueName);
    }

    @Override
    public Topic createTopic(String topicName) {
        return delegate.createTopic(topicName);
    }

    @Override
    public JMSConsumer createDurableConsumer(Topic topic, String name) {
        return delegate.createDurableConsumer(topic, name);
    }

    @Override
    public JMSConsumer createDurableConsumer(Topic topic, String name, String messageSelector, boolean noLocal) {
        return delegate.createDurableConsumer(topic, name, messageSelector, noLocal);
    }

    @Override
    public JMSConsumer createSharedDurableConsumer(Topic topic, String name) {
        return delegate.createSharedDurableConsumer(topic, name);
    }

    @Override
    public JMSConsumer createSharedDurableConsumer(Topic topic, String name, String messageSelector) {
        return delegate.createSharedDurableConsumer(topic, name, messageSelector);
    }

    @Override
    public JMSConsumer createSharedConsumer(Topic topic, String sharedSubscriptionName) {
        return delegate.createSharedConsumer(topic, sharedSubscriptionName);
    }

    @Override
    public JMSConsumer createSharedConsumer(Topic topic, String sharedSubscriptionName, String messageSelector) {
        return delegate.createSharedConsumer(topic, sharedSubscriptionName, messageSelector);
    }

    @Override
    public QueueBrowser createBrowser(Queue queue) {
        return delegate.createBrowser(queue);
    }

    @Override
    public QueueBrowser createBrowser(Queue queue, String messageSelector) {
        return delegate.createBrowser(queue, messageSelector);
    }

    @Override
    public TemporaryQueue createTemporaryQueue() {
        return delegate.createTemporaryQueue();
    }

    @Override
    public TemporaryTopic createTemporaryTopic() {
        return delegate.createTemporaryTopic();
    }

    @Override
    public void unsubscribe(String name) {
        delegate.unsubscribe(name);
    }

    @Override
    public void acknowledge() {
        delegate.acknowledge();
    }

    private final JMSContext delegate;
}
