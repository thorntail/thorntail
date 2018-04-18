package io.thorntail.vertx.impl.opentracing;

import java.util.Iterator;
import java.util.Map;

import io.opentracing.propagation.TextMap;
import io.vertx.core.eventbus.Message;

/**
 * Created by bob on 2/22/18.
 */
public class MessageAdapter implements TextMap {

    public MessageAdapter(Message<?> message) {
        this.message = message;
    }

    @Override
    public Iterator<Map.Entry<String, String>> iterator() {
        return this.message.headers().iterator();
    }

    @Override
    public void put(String key, String value) {
        this.message.headers().add(key, value);
    }

    private final Message<?> message;
}
