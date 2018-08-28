/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
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
package org.wildfly.swarm.microprofile.metrics.runtime;

import io.smallrye.metrics.MetricsRequestHandler;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.HeaderValues;
import io.undertow.util.Headers;
import io.undertow.util.HttpString;
import org.jboss.logging.Logger;

import java.util.concurrent.CountDownLatch;

/**
 * @author hrupp
 */
@SuppressWarnings("unused")
public class MetricsHttpHandler implements HttpHandler {

    private static Logger LOG = Logger.getLogger("org.wildfly.swarm.microprofile.metrics");
    private ThreadLocal<CountDownLatch> dispatched = new ThreadLocal<>();

    private HttpHandler next;
    private final MetricsRequestHandler metricsHandler = new MetricsRequestHandler();

    public MetricsHttpHandler(HttpHandler next) {
        this.next = next;
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {

        String requestPath = exchange.getRequestPath();

        if (dispatched.get() != null && dispatched.get().getCount() == 1) {
            next.handleRequest(exchange);
            dispatched.set(null);
            return;
        }

        if (!requestPath.startsWith("/metrics")) {
            next.handleRequest(exchange);
            return;
        }

        String method = exchange.getRequestMethod().toString();
        HeaderValues acceptHeaders = exchange.getRequestHeaders().get(Headers.ACCEPT);
        metricsHandler.handleRequest(requestPath, method, acceptHeaders == null ? null : acceptHeaders.stream(), (status, message, headers) -> {
            exchange.setStatusCode(status);
            headers.forEach(
                    (key, value) -> exchange.getResponseHeaders().put(new HttpString(key), value)
            );
            exchange.getResponseSender().send(message);
        });

    }
}
