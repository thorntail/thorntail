package io.thorntail.vertx;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class VertxPropertiesTest {

    @Test
    public void testGetPropertyName() {
        assertEquals("vertx.clustered", VertxProperties.getPropertyName("setClustered", VertxProperties.PROPERTY_PREFIX));
        assertEquals("vertx.cluster.host", VertxProperties.getPropertyName("setClusterHost", VertxProperties.PROPERTY_PREFIX));
        assertEquals("vertx.worker.pool.size", VertxProperties.getPropertyName("setWorkerPoolSize", VertxProperties.PROPERTY_PREFIX));
        assertEquals("vertx.blocked.thread.check.interval", VertxProperties.getPropertyName("setBlockedThreadCheckInterval", VertxProperties.PROPERTY_PREFIX));
    }

}
