package io.thorntail.testsuite.websockets;

import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.server.ServerEndpoint;

/**
 * Created by bob on 4/6/18.
 */
@ServerEndpoint("/")
public class EchoServer {

    @OnOpen
    public void onOpen() {
    }

    @OnClose
    public void onClose() {
    }

    @OnMessage
    public String onMessage(String message) {
        return message.toUpperCase();
    }
}
