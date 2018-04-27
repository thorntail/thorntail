package io.thorntail.websockets;

/**
 * Created by bob on 4/13/18.
 */
public class WebSocketMetaData {

    public WebSocketMetaData(Class<?> endpoint) {
        this.endpoint = endpoint;
    }

    public Class<?> getEndpoint() {
        return this.endpoint;
    }

    private final Class<?> endpoint;
}
