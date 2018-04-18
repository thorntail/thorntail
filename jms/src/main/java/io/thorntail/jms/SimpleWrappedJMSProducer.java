package io.thorntail.jms;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

import javax.jms.BytesMessage;
import javax.jms.CompletionListener;
import javax.jms.Destination;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.JMSProducer;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageFormatRuntimeException;
import javax.jms.ObjectMessage;
import javax.jms.TextMessage;

/**
 * Created by bob on 2/21/18.
 */
public class SimpleWrappedJMSProducer implements JMSProducer {

    protected SimpleWrappedJMSProducer(JMSContext context, JMSProducer delegate) {
        this.context = context;
        this.delegate = delegate;
    }

    protected JMSContext getContext() {
        return this.context;
    }

    protected JMSProducer getDelegate() {
        return delegate;
    }

    @Override
    public JMSProducer send(Destination destination, Message message) {
        return delegate.send(destination, message);
    }

    @Override
    public JMSProducer send(Destination destination, String body) {
        TextMessage message = getContext().createTextMessage(body);
        send( destination, message );
        return this;
    }

    @Override
    public JMSProducer send(Destination destination, Map<String, Object> body) {
        MapMessage message = getContext().createMapMessage();
        if (body != null) {
            try {
                for (Map.Entry<String, Object> entry : body.entrySet()) {
                    final String name = entry.getKey();
                    final Object v = entry.getValue();
                    if (v instanceof String) {
                        message.setString(name, (String) v);
                    } else if (v instanceof Long) {
                        message.setLong(name, (Long) v);
                    } else if (v instanceof Double) {
                        message.setDouble(name, (Double) v);
                    } else if (v instanceof Integer) {
                        message.setInt(name, (Integer) v);
                    } else if (v instanceof Character) {
                        message.setChar(name, (Character) v);
                    } else if (v instanceof Short) {
                        message.setShort(name, (Short) v);
                    } else if (v instanceof Boolean) {
                        message.setBoolean(name, (Boolean) v);
                    } else if (v instanceof Float) {
                        message.setFloat(name, (Float) v);
                    } else if (v instanceof Byte) {
                        message.setByte(name, (Byte) v);
                    } else if (v instanceof byte[]) {
                        byte[] array = (byte[]) v;
                        message.setBytes(name, array, 0, array.length);
                    } else {
                        message.setObject(name, v);
                    }
                }
            } catch (JMSException e) {
                throw new MessageFormatRuntimeException(e.getMessage());
            }
        }
        send(destination, message);
        return this;
    }

    @Override
    public JMSProducer send(Destination destination, byte[] body) {
        BytesMessage message = getContext().createBytesMessage();
        if (body != null) {
            try {
                message.writeBytes(body);
            } catch (JMSException e) {
                throw new MessageFormatRuntimeException(e.getMessage());
            }
        }
        send(destination, message);
        return this;
    }

    @Override
    public JMSProducer send(Destination destination, Serializable body) {
        ObjectMessage message = getContext().createObjectMessage(body);
        send(destination, message);
        return this;
    }

    @Override
    public JMSProducer setDisableMessageID(boolean value) {
        return delegate.setDisableMessageID(value);
    }

    @Override
    public boolean getDisableMessageID() {
        return delegate.getDisableMessageID();
    }

    @Override
    public JMSProducer setDisableMessageTimestamp(boolean value) {
        return delegate.setDisableMessageTimestamp(value);
    }

    @Override
    public boolean getDisableMessageTimestamp() {
        return delegate.getDisableMessageTimestamp();
    }

    @Override
    public JMSProducer setDeliveryMode(int deliveryMode) {
        return delegate.setDeliveryMode(deliveryMode);
    }

    @Override
    public int getDeliveryMode() {
        return delegate.getDeliveryMode();
    }

    @Override
    public JMSProducer setPriority(int priority) {
        return delegate.setPriority(priority);
    }

    @Override
    public int getPriority() {
        return delegate.getPriority();
    }

    @Override
    public JMSProducer setTimeToLive(long timeToLive) {
        return delegate.setTimeToLive(timeToLive);
    }

    @Override
    public long getTimeToLive() {
        return delegate.getTimeToLive();
    }

    @Override
    public JMSProducer setDeliveryDelay(long deliveryDelay) {
        return delegate.setDeliveryDelay(deliveryDelay);
    }

    @Override
    public long getDeliveryDelay() {
        return delegate.getDeliveryDelay();
    }

    @Override
    public JMSProducer setAsync(CompletionListener completionListener) {
        return delegate.setAsync(completionListener);
    }

    @Override
    public CompletionListener getAsync() {
        return delegate.getAsync();
    }

    @Override
    public JMSProducer setProperty(String name, boolean value) {
        return delegate.setProperty(name, value);
    }

    @Override
    public JMSProducer setProperty(String name, byte value) {
        return delegate.setProperty(name, value);
    }

    @Override
    public JMSProducer setProperty(String name, short value) {
        return delegate.setProperty(name, value);
    }

    @Override
    public JMSProducer setProperty(String name, int value) {
        return delegate.setProperty(name, value);
    }

    @Override
    public JMSProducer setProperty(String name, long value) {
        return delegate.setProperty(name, value);
    }

    @Override
    public JMSProducer setProperty(String name, float value) {
        return delegate.setProperty(name, value);
    }

    @Override
    public JMSProducer setProperty(String name, double value) {
        return delegate.setProperty(name, value);
    }

    @Override
    public JMSProducer setProperty(String name, String value) {
        return delegate.setProperty(name, value);
    }

    @Override
    public JMSProducer setProperty(String name, Object value) {
        return delegate.setProperty(name, value);
    }

    @Override
    public JMSProducer clearProperties() {
        return delegate.clearProperties();
    }

    @Override
    public boolean propertyExists(String name) {
        return delegate.propertyExists(name);
    }

    @Override
    public boolean getBooleanProperty(String name) {
        return delegate.getBooleanProperty(name);
    }

    @Override
    public byte getByteProperty(String name) {
        return delegate.getByteProperty(name);
    }

    @Override
    public short getShortProperty(String name) {
        return delegate.getShortProperty(name);
    }

    @Override
    public int getIntProperty(String name) {
        return delegate.getIntProperty(name);
    }

    @Override
    public long getLongProperty(String name) {
        return delegate.getLongProperty(name);
    }

    @Override
    public float getFloatProperty(String name) {
        return delegate.getFloatProperty(name);
    }

    @Override
    public double getDoubleProperty(String name) {
        return delegate.getDoubleProperty(name);
    }

    @Override
    public String getStringProperty(String name) {
        return delegate.getStringProperty(name);
    }

    @Override
    public Object getObjectProperty(String name) {
        return delegate.getObjectProperty(name);
    }

    @Override
    public Set<String> getPropertyNames() {
        return delegate.getPropertyNames();
    }

    @Override
    public JMSProducer setJMSCorrelationIDAsBytes(byte[] correlationID) {
        return delegate.setJMSCorrelationIDAsBytes(correlationID);
    }

    @Override
    public byte[] getJMSCorrelationIDAsBytes() {
        return delegate.getJMSCorrelationIDAsBytes();
    }

    @Override
    public JMSProducer setJMSCorrelationID(String correlationID) {
        return delegate.setJMSCorrelationID(correlationID);
    }

    @Override
    public String getJMSCorrelationID() {
        return delegate.getJMSCorrelationID();
    }

    @Override
    public JMSProducer setJMSType(String type) {
        return delegate.setJMSType(type);
    }

    @Override
    public String getJMSType() {
        return delegate.getJMSType();
    }

    @Override
    public JMSProducer setJMSReplyTo(Destination replyTo) {
        return delegate.setJMSReplyTo(replyTo);
    }

    @Override
    public Destination getJMSReplyTo() {
        return delegate.getJMSReplyTo();
    }

    private final JMSContext context;
    private final JMSProducer delegate;

}
