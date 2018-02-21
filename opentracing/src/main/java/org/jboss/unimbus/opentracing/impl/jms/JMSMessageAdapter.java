package org.jboss.unimbus.opentracing.impl.jms;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageFormatException;

import io.opentracing.propagation.TextMap;

/**
 * Created by bob on 2/21/18.
 */
class JMSMessageAdapter implements TextMap {

    JMSMessageAdapter(Message message) {
        this.message = message;
    }

    @Override
    public Iterator<Map.Entry<String, String>> iterator() {
        try {
            Map<String,String> map = new HashMap<>();
            Enumeration names = this.message.getPropertyNames();
            while ( names.hasMoreElements() ) {
                String each = names.nextElement().toString();
                try {
                    map.put(each, this.message.getStringProperty(each));
                } catch (MessageFormatException e) {
                    // ignore
                }
            }
            return map.entrySet().iterator();
        } catch (JMSException e) {
            e.printStackTrace();
        }
        return Collections.emptyIterator();
    }

    @Override
    public void put(String key, String value) {
        try {
            this.message.setStringProperty(key,value);
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    private final Message message;
}
