package io.thorntail.vertx.impl.opentracing;

import java.util.Iterator;
import java.util.Map;

import io.opentracing.propagation.TextMap;
import io.vertx.core.eventbus.DeliveryOptions;

/**
 * Created by bob on 3/1/18.
 */
public class DeliveryOptionsAdapter implements TextMap {

    public DeliveryOptionsAdapter(DeliveryOptions options) {
        this.options = options;
    }

    @Override
    public Iterator<Map.Entry<String, String>> iterator() {
        return this.options.getHeaders().iterator();
    }

    @Override
    public void put(String key, String value) {
        this.options.addHeader(key, value);
    }

    private final DeliveryOptions options;
}
