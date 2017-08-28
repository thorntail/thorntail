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

import io.undertow.client.ClientCallback;
import io.undertow.client.ClientExchange;
import io.undertow.client.ClientResponse;
import io.undertow.server.Connectors;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.ServerConnection;
import io.undertow.server.protocol.http.HttpServerConnection;
import io.undertow.util.AttachmentKey;
import io.undertow.util.Headers;
import io.undertow.util.HttpString;
import io.undertow.util.Protocols;
import io.undertow.util.StringReadChannelListener;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.jboss.logging.Logger;
import org.wildfly.swarm.monitor.HealthMetaData;
import org.wildfly.swarm.monitor.api.Monitor;
import org.xnio.ChannelListeners;
import org.xnio.IoUtils;
import org.xnio.OptionMap;
import org.xnio.Options;
import org.xnio.Xnio;
import org.xnio.XnioWorker;
import org.xnio.channels.StreamSinkChannel;

import javax.enterprise.inject.Vetoed;
import javax.naming.NamingException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

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

        //System.out.println(exchange.getRequestPath() +" on "+Thread.currentThread());

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

        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
        exchange.getResponseSender().send(sb.toString());

        exchange.endExchange();
    }

   /* private void proxyRequests(HttpServerExchange exchange) {

        if (monitor.getHealthURIs().isEmpty()) {
            noHealthEndpoints(exchange);
        } else {

            try {
                final List<InVMResponse> responses = new CopyOnWriteArrayList<>();
                CountDownLatch latch = new CountDownLatch(monitor.getHealthURIs().size());
                dispatched.set(latch);

                for (HealthMetaData healthCheck : monitor.getHealthURIs()) {
                    invokeHealthInVM(exchange, healthCheck, responses, latch);
                }

                latch.await(10, TimeUnit.SECONDS);

                if (latch.getCount() > 0) {
                    throw new Exception("Probe timed out");
                }


                boolean failed = false;
                if (!responses.isEmpty()) {

                    if (responses.size() != monitor.getHealthURIs().size()) {
                        throw new RuntimeException("The number of responses does not match!");
                    }

                    StringBuffer sb = new StringBuffer("{");
                    sb.append("\"checks\": [\n");

                    int i = 0;
                    for (InVMResponse resp : responses) {

                        sb.append(resp.getPayload());

                        if (!failed) {
                            failed = resp.getStatus() != 200;
                        }

                        if (i < responses.size() - 1) {
                            sb.append(",\n");
                        }
                        i++;
                    }
                    sb.append("],\n");

                    String outcome = failed ? "DOWN" : "UP"; // we don't have policies yet, so keep it simple
                    sb.append("\"outcome\": \"" + outcome + "\"\n");
                    sb.append("}\n");

                    // send a response
                    if (failed) {
                        exchange.setStatusCode(503);
                    }

                    exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
                    exchange.getResponseSender().send(sb.toString());

                } else {
                    new RuntimeException("Responses should not be empty").printStackTrace();
                    exchange.setStatusCode(500);
                }

                exchange.endExchange();


            } catch (Throwable t) {
                LOG.error("Health check failed", t);

                if (!exchange.isResponseStarted()) {
                    exchange.setStatusCode(500);
                }
                exchange.endExchange();
            }

        }

    }*/

    private void invokeHealthInVM(final HttpServerExchange exchange, HealthMetaData healthCheck, List<InVMResponse> responses, CountDownLatch latch) {
        try {

            String delegateContext = healthCheck.getWebContext();

            final InVMConnection connection = new InVMConnection(
                    worker,
                    exchange.getConnection().getLocalAddress(InetSocketAddress.class).getPort()
            );
            final HttpServerExchange mockExchange = new HttpServerExchange(connection);
            mockExchange.setRequestScheme("http");
            mockExchange.setRequestMethod(new HttpString("GET"));
            mockExchange.setProtocol(Protocols.HTTP_1_0);
            mockExchange.setRequestURI(delegateContext);
            mockExchange.setRequestPath(delegateContext);
            mockExchange.setRelativePath(delegateContext);
            mockExchange.getRequestHeaders().add(Headers.HOST, exchange.getRequestHeaders().get(Headers.HOST).getFirst());
            mockExchange.putAttachment(TOKEN, EPHEMERAL_TOKEN);
            mockExchange.putAttachment(RESPONSES, responses);
            connection.addCloseListener(new ServerConnection.CloseListener() {
                @Override
                public void closed(ServerConnection connection) {
                    LOG.trace("Mock connection closed");
                    StringBuffer sb = new StringBuffer();
                    ((InVMConnection) connection).flushTo(sb);
                    LOG.trace("Response payload: " + sb.toString());
                    if ("application/json".equals(mockExchange.getResponseHeaders().getFirst(Headers.CONTENT_TYPE))) {
                        responses.add(new InVMResponse(mockExchange.getStatusCode(), sb.toString()));
                    } else {
                        StringBuffer json = new StringBuffer(LCURL);
                        json.append("\"name\"").append(":\"").append(mockExchange.getRelativePath()).append("\",");
                        json.append("\"state\"").append(":\"").append("DOWN").append("\",");
                            json.append("\"data\"").append(":").append(LCURL);
                                json.append("\"status-code\"").append(":").append(mockExchange.getStatusCode());
                            json.append(RCURL);
                        json.append(RCURL);

                        responses.add(new InVMResponse(mockExchange.getStatusCode(), json.toString()));
                    }

                    mockExchange.removeAttachment(RESPONSES);
                    IoUtils.safeClose(connection);
                    latch.countDown();
                }
            });

            HttpServerConnection httpConnection = (HttpServerConnection) exchange.getConnection();
            mockExchange.startBlocking();
            Connectors.executeRootHandler(httpConnection.getRootHandler(), mockExchange);


        } catch (Throwable t) {
            LOG.error("Health check failed", t);
            latch.countDown();
        }
    }

    private static final AttachmentKey<String> RESPONSE_BODY = AttachmentKey.create(String.class);

    private ClientCallback<ClientExchange> createClientCallback(final List<ClientResponse> responses, CountDownLatch latch) {

        return new ClientCallback<ClientExchange>() {
            @Override
            public void completed(final ClientExchange result) {
                result.setResponseListener(new ClientCallback<ClientExchange>() {
                    @Override
                    public void completed(final ClientExchange result) {
                        responses.add(result.getResponse());
                        new StringReadChannelListener(result.getConnection().getBufferPool()) {

                            @Override
                            protected void stringDone(String string) {
                                result.getResponse().putAttachment(RESPONSE_BODY, string);
                                latch.countDown();
                            }

                            @Override
                            protected void error(IOException e) {
                                LOG.error("Failed to read response", e);
                                latch.countDown();

                            }
                        }.setup(result.getResponseChannel());
                    }

                    @Override
                    public void failed(IOException e) {
                        LOG.error("Failed to read response", e);
                        latch.countDown();
                    }
                });

                try {
                    result.getRequestChannel().shutdownWrites();
                    if (!result.getRequestChannel().flush()) {
                        result.getRequestChannel().getWriteSetter().set(ChannelListeners.<StreamSinkChannel>flushingChannelListener(null, null));
                        result.getRequestChannel().resumeWrites();
                    }
                } catch (IOException e) {
                    LOG.error("Failed to read response", e);
                    latch.countDown();
                }
            }

            @Override
            public void failed(IOException e) {
                LOG.error("Probe invocation failed", e);
                latch.countDown();
            }
        };
    }

    private void noHealthEndpoints(HttpServerExchange exchange) {
        exchange.setStatusCode(200);
        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
        exchange.getResponseSender().send("{\"outcome\":\"UP\", \"checks\":[]}");
        exchange.endExchange();
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

    private static Logger LOG = Logger.getLogger("org.wildfly.swarm.monitor.health");

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
