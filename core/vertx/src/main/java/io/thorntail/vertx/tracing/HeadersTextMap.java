package io.thorntail.vertx.tracing;

import java.util.Iterator;
import java.util.Map.Entry;

import io.opentracing.propagation.TextMap;
import io.vertx.core.MultiMap;

/**
 *
 * @author Martin Kouba
 */
public class HeadersTextMap implements TextMap {

    private final MultiMap headers;

    public HeadersTextMap(MultiMap headers) {
        this.headers = headers;
    }

    @Override
    public Iterator<Entry<String, String>> iterator() {
        return headers.iterator();
    }

    @Override
    public void put(String key, String value) {
        headers.add(key, value);
    }

}
