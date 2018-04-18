package io.thorntail.websockets.ext;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.enterprise.inject.spi.WithAnnotations;
import javax.websocket.server.ServerEndpoint;

import io.thorntail.websockets.WebSocketMetaData;

/**
 * Created by bob on 4/13/18.
 */
public class WebSocketsExtension implements Extension {

    <T> void process(@Observes @WithAnnotations({ServerEndpoint.class}) ProcessAnnotatedType<T> event) {
        this.meta.add(new WebSocketMetaData(event.getAnnotatedType().getJavaClass()));
    }

    public List<WebSocketMetaData> getMetaData() {
        return this.meta;
    }

    private List<WebSocketMetaData> meta = new ArrayList<>();
}
