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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

import javax.enterprise.inject.Vetoed;
import javax.naming.NamingException;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.AttachmentKey;
import io.undertow.util.Headers;
import io.undertow.util.HttpString;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.jboss.logging.Logger;
import org.wildfly.swarm.microprofile.health.api.Monitor;
import org.xnio.OptionMap;
import org.xnio.Options;
import org.xnio.Xnio;
import org.xnio.XnioWorker;

/**
 * The actual monitoring HTTP endpoints. These are wrapped by {@link SecureHttpContexts}.
 *
 * @author Heiko Braun
 */
@Vetoed
public class HttpContexts implements HttpHandler {


    public static final String LCURL = "{";
    public static final String RCURL = "}";

    protected ThreadLocal<CountDownLatch> dispatched = new ThreadLocal<>();

    private AttachmentKey<List> RESPONSES = AttachmentKey.create(List.class);

    static AttachmentKey<String> TOKEN = AttachmentKey.create(String.class);

    public HttpContexts(HttpHandler next) {

        try {
            this.worker = Xnio.getInstance().createWorker(
                    OptionMap.builder()
                            .set(Options.WORKER_IO_THREADS, 5)
                            .set(Options.WORKER_TASK_CORE_THREADS, 5)
                            .set(Options.WORKER_TASK_MAX_THREADS, 10)
                            .set(Options.TCP_NODELAY, true)
                            .getMap()
            );

        } catch (IOException e) {
            throw new IllegalStateException("Failed to create worker pool");
        }
        this.next = next;

        try {
            this.monitor = Monitor.lookup();
        } catch (NamingException e) {
            throw new RuntimeException("Failed to lookup monitor", e);
        }

    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {

        if (dispatched.get() != null && dispatched.get().getCount() == 1) {
            next.handleRequest(exchange);
            dispatched.set(null);
            return;
        }

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
            proxyRequestsCDI(exchange);
            return;
        }

        next.handleRequest(exchange);
    }

    private void proxyRequestsCDI(HttpServerExchange exchange) {

        Set<Object> procedures = monitor.getHealthDelegates();

        if (procedures.isEmpty()) {
            noHealthEndpoints(exchange);
            return;
        }

        List<org.eclipse.microprofile.health.HealthCheckResponse> responses = new ArrayList<>();

        for (Object procedure : procedures) {
            org.eclipse.microprofile.health.HealthCheckResponse status = ((HealthCheck)procedure).call();
            responses.add(status);
        }

        StringBuffer sb = new StringBuffer(LCURL);
        sb.append("\"checks\": [\n");

        int i = 0;
        boolean failed = false;

        for (org.eclipse.microprofile.health.HealthCheckResponse resp : responses) {

            sb.append(toJson(resp));

            if (!failed) {
                failed = resp.getState() != HealthCheckResponse.State.UP;
            }

            if (i < responses.size() - 1) {
                sb.append(",\n");
            }
            i++;
        }
        sb.append("],\n");

        String outcome = failed ? "DOWN" : "UP";
        sb.append("\"outcome\": \"" + outcome + "\"\n");
        sb.append("}\n");

        // send a response
        if (failed) {
            exchange.setStatusCode(503);
        }

        responseHeaders(exchange);
        exchange.getResponseSender().send(sb.toString());
        exchange.endExchange();

    }

    private void responseHeaders(HttpServerExchange exchange) {
        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
        exchange.getResponseHeaders().put(new HttpString("Access-Control-Allow-Origin"), "*");
        exchange.getResponseHeaders().put(new HttpString("Access-Control-Allow-Headers"), "origin, content-type, accept, authorization");
        exchange.getResponseHeaders().put(new HttpString("Access-Control-Allow-Credentials"), "true");
        exchange.getResponseHeaders().put(new HttpString("Access-Control-Allow-Methods"), "GET, POST, PUT, DELETE, OPTIONS, HEAD");
        exchange.getResponseHeaders().put(new HttpString("Access-Control-Max-Age"), "1209600");
    }

    private static final AttachmentKey<String> RESPONSE_BODY = AttachmentKey.create(String.class);
    private void noHealthEndpoints(HttpServerExchange exchange) {
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

    public static String toJson(HealthCheckResponse status) {
        StringBuilder sb = new StringBuilder();
        sb.append(LCURL);
        sb.append(QUOTE).append("name").append("\":\"").append(status.getName()).append("\",");
        sb.append(QUOTE).append("state").append("\":\"").append(status.getState().name()).append(QUOTE);
        if (status.getData().isPresent()) {
            sb.append(",");
            sb.append(QUOTE).append(DATA).append("\": {");
            Map<String, Object> atts = status.getData().get();
            int i = 0;
            for (String key : atts.keySet()) {
                sb.append(QUOTE).append(key).append("\":").append(encode(atts.get(key)));
                if (i < atts.keySet().size() - 1) {
                    sb.append(",");
                }
                i++;
            }
            sb.append(RCURL);
        }

        sb.append(RCURL);
        return sb.toString();
    }

    private static String encode(Object o) {
        String res = null;
        if (o instanceof String) {
            res = "\"" + o.toString() + "\"";
        } else {
            res = o.toString();
        }

        return res;
    }


    public static List<String> getDefaultContextNames() {
        return Arrays.asList(NODE, HEAP, HEALTH, THREADS);
    }

    private static Logger LOG = Logger.getLogger("org.wildfly.swarm.health");

    public static final String NODE = "/node";

    public static final String HEAP = "/heap";

    public static final String THREADS = "/threads";

    public static final String HEALTH = "/health";

    static final String EPHEMERAL_TOKEN = UUID.randomUUID().toString();

    private final Monitor monitor;

    private final HttpHandler next;

    private XnioWorker worker;

    private static final String ID = "id";

    private static final String RESULT = "result";

    private static final String DATA = "data";

    public static final String QUOTE = "\"";

    class InVMResponse {
        private int status;

        private String payload;

        public InVMResponse(int status, String payload) {
            this.status = status;
            this.payload = payload;
        }

        public int getStatus() {
            return status;
        }

        public String getPayload() {
            return payload;
        }
    }
}
