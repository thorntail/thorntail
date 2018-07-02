package io.thorntail.vertx.web.tracing;

import java.util.Iterator;
import java.util.Map;

import io.opentracing.propagation.TextMap;
import io.vertx.ext.web.RoutingContext;

/**
 *
 * @author Martin Kouba
 */
public class RoutingContextTextMap implements TextMap {

    private final RoutingContext ctx;

    public RoutingContextTextMap(RoutingContext ctx) {
        this.ctx = ctx;
    }

    @Override
    public Iterator<Map.Entry<String, String>> iterator() {
        return ctx.request().headers().iterator();
    }

    @Override
    public void put(String key, String value) {
        ctx.request().headers().add(key, value);
    }

}
