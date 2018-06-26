package io.thorntail.vertx.tracing;

import java.util.Iterator;
import java.util.Map;

import io.opentracing.propagation.TextMap;
import io.thorntail.vertx.VertxMessage;

/**
 *
 * @author Martin Kouba
 */
public class VertxMessageTextMap implements TextMap {

    private final VertxMessage message;

    public VertxMessageTextMap(VertxMessage message) {
        this.message = message;
    }

    @Override
    public Iterator<Map.Entry<String, String>> iterator() {
        return message.headers().iterator();
    }

    @Override
    public void put(String key, String value) {
        message.headers().add(key, value);
    }

}
