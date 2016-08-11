/**
 * Copyright 2015-2016 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wildfly.swarm.monitor.runtime;

import java.util.Arrays;
import java.util.List;

import javax.enterprise.inject.Vetoed;
import javax.naming.NamingException;

import io.undertow.attribute.ReadOnlyAttributeException;
import io.undertow.attribute.RelativePathAttribute;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import org.jboss.dmr.ModelNode;
import org.wildfly.swarm.monitor.HealthMetaData;

/**
 * The actual monitoring HTTP endpoints. These are wrapped by {@link SecureHttpContexts}.
 *
 * @author Heiko Braun
 */
@Vetoed
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
            return;
        } else if (HEAP.equals(exchange.getRequestPath())) {
            heap(exchange);
            return;
        } else if (THREADS.equals(exchange.getRequestPath())) {
            threads(exchange);
            return;
        }
        else if(Queries.preventDirectAccess(monitor, exchange.getRelativePath()))
        {
            exchange.setStatusCode(403);
            exchange.endExchange();
            return;
        }
        else if (HEALTH.equals(exchange.getRequestPath())) {
            listHealtSubresources(exchange);
            return;
        }
        else if (exchange.getRelativePath().startsWith(HEALTH)) {
            healthRedirect(exchange);
            if(exchange.isResponseStarted()) // allow the redirect handler to proceed
                return;
        }

        next.handleRequest(exchange);
    }

    private void listHealtSubresources(HttpServerExchange exchange) {
        if(monitor.getHealthURIs().isEmpty()) {
            noHealthEndpoints(exchange);
        }
        else
        {
            ModelNode payload = new ModelNode();
            for (HealthMetaData endpoint : monitor.getHealthURIs()) {
                payload.get("links").add(HEALTH + endpoint.getWebContext());
            }

            exchange.setStatusCode(200);
            exchange.getResponseSender().send(payload.toJSONString(false));
        }

    }

    private void noHealthEndpoints(HttpServerExchange exchange) {
        exchange.setStatusCode(503);
        exchange.getResponseSender().send("No health endpoints configured!");
    }

    private void healthRedirect(HttpServerExchange exchange) {
        if(monitor.getHealthURIs().isEmpty()) {
            noHealthEndpoints(exchange);
        }
        else {

            String rel = exchange.getRelativePath();
            String subresource = rel.substring(HEALTH.length(), rel.length());
            boolean matches = false;
            for (HealthMetaData metaData : monitor.getHealthURIs()) {
                if(metaData.getWebContext().equals(subresource)) {
                    matches = true;
                    break;
                }
            }
            if(matches) {
                try {
                    RelativePathAttribute.INSTANCE.writeAttribute(exchange, subresource);
                } catch (ReadOnlyAttributeException e) {
                    e.printStackTrace();
                }
            }
            else {
                exchange.setStatusCode(404);
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

    public static final String NODE = "/node";

    public static final String HEAP = "/heap";

    public static final String THREADS = "/threads";

    public static final String HEALTH = "/health";

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
