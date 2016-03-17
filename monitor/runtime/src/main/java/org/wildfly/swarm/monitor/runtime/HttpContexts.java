package org.wildfly.swarm.monitor.runtime;

import java.util.Arrays;
import java.util.List;

import javax.naming.NamingException;

import io.undertow.attribute.ReadOnlyAttributeException;
import io.undertow.attribute.RelativePathAttribute;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;

/**
 * The actual monitoring HTTP endpoints. These are wrapped by {@link SecureHttpContexts}.
 *
 * @author Heiko Braun
 */
class HttpContexts implements HttpHandler {

    public HttpContexts(HttpHandler next) {
        this.next = next;

        try {
            this.monitor = Monitor.lookup();
        } catch (NamingException e) {
            throw new RuntimeException("Failed to lookup monitor", e);
        }

    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {

        if (NODE.equals(exchange.getRequestPath())) {
            nodeInfo(exchange);
        } else if (HEAP.equals(exchange.getRequestPath())) {
            heap(exchange);
        } else if (THREADS.equals(exchange.getRequestPath())) {
            threads(exchange);
        } else if (HEALTH.equals(exchange.getRequestPath())) {
            healthRedirect(exchange);
        }
        else if(Queries.isSecuredHealthEndpoint(monitor, exchange.getRelativePath()))
        {
            exchange.setStatusCode(403);
            exchange.endExchange();
        }

        next.handleRequest(exchange);
    }

    private void healthRedirect(HttpServerExchange exchange) {
        if(monitor.getHealthURIs().isEmpty()) {
            exchange.setStatusCode(503);
            exchange.getResponseSender().send("No health endpoints configured!");
        }
        else {

            // TODO: Does this need to be guarded for concurrent access?
            if(roundRobin==null)
                roundRobin = new RoundRobin(monitor.getHealthURIs());

            try {
                RelativePathAttribute.INSTANCE.writeAttribute(exchange, roundRobin.next());
            } catch (ReadOnlyAttributeException e) {
                e.printStackTrace();
            }
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

    public static List<String> getDefaultContextNames() {
        List<String> contexts= Arrays.asList(new String[]{NODE,HEAP,HEALTH,THREADS});
        return contexts;
    };

    private static final String NODE = "/node";

    private static final String HEAP = "/heap";

    private static final String THREADS = "/threads";

    private static final String HEALTH = "/health";

    private final Monitor monitor;

    class RoundRobin {
        private final List<HealthMetaData> contexts;
        private int pos = 0;

        public RoundRobin(List<HealthMetaData> contexts) {
            this.contexts = contexts;
        }

        String next() {
            if(pos>=contexts.size())
                pos = 0;

            String next = contexts.get(pos).getWebContext();
            pos++;
            return next;
        }
    }

    private final HttpHandler next;

    private RoundRobin roundRobin;
}
