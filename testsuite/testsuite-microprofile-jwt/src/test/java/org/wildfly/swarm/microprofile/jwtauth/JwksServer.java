package org.wildfly.swarm.microprofile.jwtauth;

import com.sun.net.httpserver.HttpServer;

import javax.json.Json;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

final class JwksServer {
    private final KeyTool keyTool;
    private final int port;
    private HttpServer httpServer;

    JwksServer(final KeyTool keyTool, final int port) {
        this.keyTool = keyTool;
        this.port = port;
    }

    synchronized void start() throws IOException {
        if (httpServer != null) {
            return;
        }

        httpServer = HttpServer.create(new InetSocketAddress(port), 0);
        httpServer.createContext("/jwks", httpExchange -> {
            final String jwks = Json.createObjectBuilder()
                    .add("keys", Json.createArrayBuilder().add(keyTool.getJwkObject()))
                    .build()
                    .toString();
            final byte[] jwksBytes = jwks.getBytes("UTF-8");
            httpExchange.sendResponseHeaders(200, jwks.length());
            try (final OutputStream responseBody = httpExchange.getResponseBody()) {
                responseBody.write(jwksBytes);
            }
        });

        httpServer.start();
    }

    synchronized void stop() {
        if (httpServer != null) {
            httpServer.stop(10);
        }
    }
}
