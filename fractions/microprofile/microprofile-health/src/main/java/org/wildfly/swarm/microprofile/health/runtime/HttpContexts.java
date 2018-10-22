/**
 * Copyright 2015-2017 Red Hat, Inc, and individual contributors.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package org.wildfly.swarm.microprofile.health.runtime;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import javax.enterprise.inject.Vetoed;
import javax.naming.NamingException;

import org.wildfly.security.manager.WildFlySecurityManager;
import org.wildfly.swarm.microprofile.health.api.Monitor;

import io.smallrye.health.SmallRyeHealth;
import io.smallrye.health.SmallRyeHealthReporter;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.AttachmentKey;
import io.undertow.util.Headers;
import io.undertow.util.HttpString;

/**
 * The actual monitoring HTTP endpoints. These are wrapped by {@link SecureHttpContexts}.
 *
 * @author Heiko Braun
 */
@Vetoed
public class HttpContexts implements HttpHandler {

    public static final String LCURL = "{";
    public static final String RCURL = "}";

    static AttachmentKey<String> TOKEN = AttachmentKey.create(String.class);

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
        } else if (HEALTH.equals(exchange.getRequestPath())) {
            health(exchange);
            return;
        }

        next.handleRequest(exchange);
    }

    private void responseHeaders(HttpServerExchange exchange) {
        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
        exchange.getResponseHeaders().put(new HttpString("Access-Control-Allow-Origin"), "*");
        exchange.getResponseHeaders().put(new HttpString("Access-Control-Allow-Headers"), "origin, content-type, accept, authorization");
        exchange.getResponseHeaders().put(new HttpString("Access-Control-Allow-Credentials"), "true");
        exchange.getResponseHeaders().put(new HttpString("Access-Control-Allow-Methods"), "GET, POST, PUT, DELETE, OPTIONS, HEAD");
        exchange.getResponseHeaders().put(new HttpString("Access-Control-Max-Age"), "1209600");
    }

    private void health(HttpServerExchange exchange) {
        if (monitor.getHealthReporter() != null) {
            SmallRyeHealthReporter reporter = (SmallRyeHealthReporter) monitor.getHealthReporter();
            SmallRyeHealth health;

            // THORN-2195: Use the correct TCCL when health checks are obtained
            ClassLoader oldTccl = WildFlySecurityManager.getCurrentContextClassLoaderPrivileged();
            try {
                WildFlySecurityManager.setCurrentContextClassLoaderPrivileged(monitor.getContextClassLoader());
                health = reporter.getHealth();
            } finally {
                WildFlySecurityManager.setCurrentContextClassLoaderPrivileged(oldTccl);
            }

            if (health.isDown()) {
                exchange.setStatusCode(503);
            } else {
                exchange.setStatusCode(200);
            }
            responseHeaders(exchange);
            exchange.getResponseSender().send(health.getPayload().toString());
            exchange.endExchange();
        } else {
            defaultHealthInfo(exchange);
        }
    }

    private void defaultHealthInfo(HttpServerExchange exchange) {
        exchange.setStatusCode(200);
        responseHeaders(exchange);
        exchange.getResponseSender().send("{\"outcome\":\"UP\", \"checks\":[]}");
        exchange.endExchange();
    }

    private void nodeInfo(HttpServerExchange exchange) {
        responseHeaders(exchange);
        exchange.getResponseSender().send(monitor.getNodeInfo().toJSONString(false));
    }

    private void heap(HttpServerExchange exchange) {
        responseHeaders(exchange);
        exchange.getResponseSender().send(monitor.heap().toJSONString(false));
    }

    private void threads(HttpServerExchange exchange) {
        responseHeaders(exchange);
        exchange.getResponseSender().send(monitor.threads().toJSONString(false));
    }

    public static List<String> getDefaultContextNames() {
        return Arrays.asList(NODE, HEAP, HEALTH, THREADS);
    }

    public static final String NODE = "/node";

    public static final String HEAP = "/heap";

    public static final String THREADS = "/threads";

    public static final String HEALTH = "/health";

    static final String EPHEMERAL_TOKEN = UUID.randomUUID().toString();

    private final Monitor monitor;

    private final HttpHandler next;
}
