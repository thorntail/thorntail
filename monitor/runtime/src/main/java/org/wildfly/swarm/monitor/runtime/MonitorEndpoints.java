package org.wildfly.swarm.monitor.runtime;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import org.jboss.dmr.ModelNode;

import javax.naming.NamingException;

/**
 * @author Heiko Braun
 * @since 18/02/16
 */
public class MonitorEndpoints implements HttpHandler {


    private final Monitor monitor;

    public MonitorEndpoints(HttpHandler parent) {
        try {
            this.monitor = Monitor.lookup();
        } catch (NamingException e) {
            throw new RuntimeException("Failed to lookup monitor", e);
        }
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {

        if("/node".equals(exchange.getRequestPath())) {
            nodeInfo(exchange);
        }
        else if("/heap".equals(exchange.getRequestPath())) {
            heap(exchange);
        }
        else if("/threads".equals(exchange.getRequestPath())) {
            threads(exchange);
        }

    }

    private void nodeInfo(HttpServerExchange exchange) {
        exchange.getResponseSender().send(monitor.getNodeInfo().toJSONString(false));
    }

    private void heap(HttpServerExchange exchange) {
        exchange.getResponseSender().send(monitor.heap().toJSONString(false));
    }

    private void threads(HttpServerExchange exchange) {
        exchange.getResponseSender().send(monitor.threads().toJSONString(false));
    }
}

