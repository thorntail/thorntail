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

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.HeaderValues;
import io.undertow.util.Headers;
import io.undertow.util.HttpString;
import org.eclipse.microprofile.metrics.Metric;
import org.eclipse.microprofile.metrics.MetricRegistry;
import org.jboss.logging.Logger;
import org.wildfly.swarm.microprofile.metrics.runtime.exporters.Exporter;
import org.wildfly.swarm.microprofile.metrics.runtime.exporters.JsonExporter;
import org.wildfly.swarm.microprofile.metrics.runtime.exporters.PrometheusExporter;
import org.wildfly.swarm.microprofile.metrics.runtime.exporters.JsonMetadataExporter;

import java.util.Map;
import java.util.concurrent.CountDownLatch;

/**
 * @author hrupp
 */
@SuppressWarnings("unused")
public class MetricsHttpHandler implements HttpHandler {

    private static Logger LOG = Logger.getLogger("org.wildfly.swarm.microprofile.metrics");
    private ThreadLocal<CountDownLatch> dispatched = new ThreadLocal<>();

    private HttpHandler next;

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

        // request is for us, so let's handle it

        Exporter exporter = obtainExporter(exchange);
        if (exporter == null) {
            exchange.setStatusCode(406);
            exchange.setReasonPhrase("No exporter found for method " + exchange.getRequestMethod() + " and media type");
            return;
        }

        String scopePath = requestPath.substring(8);
        if (scopePath.startsWith("/")) {
            scopePath = scopePath.substring(1);
        }
        if (scopePath.endsWith("/")) {
            scopePath = scopePath.substring(0, scopePath.length() - 1);
        }

        StringBuilder sb;

        if (scopePath.isEmpty()) {
            // All metrics

            sb = exporter.exportAllScopes();

        } else if (scopePath.contains("/")) {
            // One metric in a scope

            String attribute = scopePath.substring(scopePath.indexOf('/') + 1);

            MetricRegistry.Type scope = getScopeFromPath(exchange, scopePath.substring(0, scopePath.indexOf('/')));
            if (scope == null) {
                exchange.setStatusCode(404);
                exchange.setReasonPhrase("Scope " + scopePath + " not found");
                return;
            }

            MetricRegistry registry = MetricRegistries.get(scope);
            Map<String, Metric> metricValuesMap = registry.getMetrics();

            if (metricValuesMap.containsKey(attribute)) {
                sb = exporter.exportOneMetric(scope, attribute);
            } else {
                exchange.setStatusCode(404);
                exchange.setReasonPhrase("Metric " + scopePath + " not found");
                return;
            }
        } else {
            // A single scope

            MetricRegistry.Type scope = getScopeFromPath(exchange, scopePath);
            if (scope == null) {
                exchange.setStatusCode(404);
                exchange.setReasonPhrase("Scope " + scopePath + " not found");
                return;
            }

            MetricRegistry reg = MetricRegistries.get(scope);
            if (reg.getMetadata().size() == 0) {
                exchange.setStatusCode(204);
                exchange.setReasonPhrase("No data in scope " + scopePath);
            }

            sb = exporter.exportOneScope(scope);
        }

        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, exporter.getContentType());
        provideCorsHeaders(exchange);
        exchange.getResponseHeaders().put(new HttpString("Access-Control-Max-Age"), "1209600");
        exchange.getResponseSender().send(sb.toString());

    }

    private void provideCorsHeaders(HttpServerExchange exchange) {
        exchange.getResponseHeaders().put(new HttpString("Access-Control-Allow-Origin"), "*");
        exchange.getResponseHeaders().put(new HttpString("Access-Control-Allow-Headers"), "origin, content-type, accept, authorization");
        exchange.getResponseHeaders().put(new HttpString("Access-Control-Allow-Credentials"), "true");
        exchange.getResponseHeaders().put(new HttpString("Access-Control-Allow-Methods"), "GET, POST, PUT, DELETE, OPTIONS, HEAD");
    }

    private MetricRegistry.Type getScopeFromPath(HttpServerExchange exchange, String scopePath) {
        MetricRegistry.Type scope;
        try {
            scope = MetricRegistry.Type.valueOf(scopePath.toUpperCase());
        } catch (IllegalArgumentException iae) {
            exchange.setStatusCode(404);
            exchange.setReasonPhrase("Bad scope requested: " + scopePath);
            return null;
        }
        return scope;
    }


    /**
     * Determine which exporter we want.
     * @param exchange The http exchange coming in
     * @return An exporter instance or null in case no matching exporter existed.
     */
    private Exporter obtainExporter(HttpServerExchange exchange) {
        HeaderValues acceptHeaders = exchange.getRequestHeaders().get(Headers.ACCEPT);
        Exporter exporter;

        String method = exchange.getRequestMethod().toString();

        if (acceptHeaders == null) {
            if (method.equals("GET")) {
                exporter = new PrometheusExporter();
            } else {
                return null;
            }
        } else {
            // Header can look like "application/json, text/plain, */*"
            if (acceptHeaders.getFirst() != null && acceptHeaders.getFirst().startsWith("application/json")) {


                if (method.equals("GET")) {
                    exporter = new JsonExporter();
                } else if (method.equals("OPTIONS")) {
                    exporter = new JsonMetadataExporter();
                } else {
                    return null;
                }
            } else {
                // This is the fallback, but only for GET, as Prometheus does not support OPTIONS
                if (method.equals("GET")) {
                    exporter = new PrometheusExporter();
                } else {
                    return null;
                }
            }
        }
        return exporter;
    }


}
