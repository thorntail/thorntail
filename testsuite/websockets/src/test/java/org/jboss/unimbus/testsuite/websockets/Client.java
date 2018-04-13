package org.jboss.unimbus.testsuite.websockets;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.websocket.ClientEndpoint;
import javax.websocket.ContainerProvider;
import javax.websocket.OnMessage;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;

/**
 * Created by bob on 4/13/18.
 */
@ClientEndpoint
public class Client {

    public Client(URI serverUri) {
        try {
            WebSocketContainer container = ContainerProvider
                    .getWebSocketContainer();
            this.session = container.connectToServer(this, serverUri);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @OnMessage
    public void onMessage(String message) {
        synchronized (this) {
            this.messages.add(message);
            notifyAll();
        }
    }

    public List<String> getMessages() {
        return this.messages;
    }

    public void send(String str) {
        this.session.getAsyncRemote().sendText(str);
    }

    public synchronized  void await(int number) throws InterruptedException {
        while ( this.messages.size() < number ) {
            wait();
        }
    }

    public void close() throws IOException {
        this.session.close();
    }

    private final Session session;

    private List<String> messages = new ArrayList<>();


}
