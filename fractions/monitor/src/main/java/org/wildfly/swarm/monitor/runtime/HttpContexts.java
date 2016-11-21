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

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.enterprise.inject.Vetoed;
import javax.naming.NamingException;

import io.undertow.attribute.ReadOnlyAttributeException;
import io.undertow.attribute.RelativePathAttribute;
import io.undertow.client.ClientCallback;
import io.undertow.client.ClientConnection;
import io.undertow.client.ClientExchange;
import io.undertow.client.ClientRequest;
import io.undertow.client.ClientResponse;
import io.undertow.client.UndertowClient;
import io.undertow.server.DefaultByteBufferPool;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.AttachmentKey;
import io.undertow.util.HttpString;
import io.undertow.util.Protocols;
import io.undertow.util.SameThreadExecutor;
import io.undertow.util.StatusCodes;
import io.undertow.util.StringReadChannelListener;
import org.jboss.dmr.ModelNode;
import org.jboss.logging.Logger;
import org.wildfly.swarm.monitor.HealthMetaData;
import org.xnio.ChannelListeners;
import org.xnio.OptionMap;
import org.xnio.Options;
import org.xnio.Xnio;
import org.xnio.XnioWorker;
import org.xnio.channels.StreamSinkChannel;

/**
 * The actual monitoring HTTP endpoints. These are wrapped by {@link SecureHttpContexts}.
 *
 * @author Heiko Braun
 */
@Vetoed
class HttpContexts implements HttpHandler {

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
            proxyRequests(exchange);
            return;
        }

        next.handleRequest(exchange);
    }

    private void proxyRequests(HttpServerExchange exchange) {

        if(monitor.getHealthURIs().isEmpty()) {
            noHealthEndpoints(exchange);
        } else {

            Runnable r = new Runnable() {
                @Override
                public void run() {

                    try {

                        final UndertowClient client = UndertowClient.getInstance();
                        final List<ClientResponse> responses = new ArrayList();

                        ClientConnection connection =
                                client.connect(
                                        new URL("http://" + exchange.getHostAndPort()).toURI(),
                                        worker,
                                        new DefaultByteBufferPool(false, 1024, 100, 12),
                                        OptionMap.EMPTY
                                ).get();

                        //  probe each health endpoint
                        for(HealthMetaData healthCheck : monitor.getHealthURIs()) {

                            try {
                                ClientRequest request = new ClientRequest();
                                request.setProtocol(Protocols.HTTP_1_1);
                                request.setPath(healthCheck.getWebContext());
                                request.getRequestHeaders().add(X_SWARM_HEALTH_TOKEN, EPHEMERAL_TOKEN);
                                final CountDownLatch latch = new CountDownLatch(1);
                                connection.sendRequest(request, createClientCallback(responses, latch));
                                latch.await(monitor.getProbeTimeoutSeconds(), TimeUnit.SECONDS);

                                if(latch.getCount()>0)
                                    throw new Exception("Probing "+healthCheck.getWebContext() + " timed out");

                            } catch (Exception e) {
                                connection.close();
                                throw new RuntimeException("Failed to process health check "+ healthCheck.getWebContext(), e);
                            }
                        }

                        // process responses
                        boolean failed = false;
                        if (!responses.isEmpty()) {

                            StringBuffer sb = new StringBuffer("{");
                            sb.append("\"checks\": [\n");

                            int i=0;
                            for(ClientResponse resp : responses) {
                                if(200==resp.getResponseCode()) {
                                    sb.append(resp.getAttachment(RESPONSE_BODY));
                                } else if(503==resp.getResponseCode()){
                                    sb.append(resp.getAttachment(RESPONSE_BODY));
                                    failed = true;
                                } else {
                                    throw new RuntimeException("Unexpected status code: "+resp.getResponseCode());
                                }
                                if(i<responses.size()-1)
                                    sb.append(",\n");
                                i++;
                            }
                            sb.append("],\n");

                            String outcome = failed ? "DOWN":"UP"; // we don't have policies yet, so keep it simple
                            sb.append("\"outcome\": \""+outcome+"\"\n");
                            sb.append("}\n");

                            // send a response
                            if(failed)
                                exchange.setStatusCode(503);
                            exchange.getResponseSender().send(sb.toString());

                        } else {
                            exchange.setStatusCode(204);
                        }

                        connection.close();
                        exchange.endExchange();

                    } catch (Throwable t) {
                        LOG.error("Health check failed", t);

                        if(!exchange.isResponseStarted())
                            exchange.setStatusCode(500);
                        exchange.endExchange();
                    }
                }
            };

            exchange.dispatch(exchange.isInIoThread() ? SameThreadExecutor.INSTANCE : exchange.getIoThread(), r);

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
                    if(!result.getRequestChannel().flush()) {
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
        exchange.setStatusCode(204);
        exchange.setReasonPhrase("No health endpoints configured!");
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

    private static Logger LOG = Logger.getLogger("org.wildfly.swarm.monitor.health");

    public static final String NODE = "/node";

    public static final String HEAP = "/heap";

    public static final String THREADS = "/threads";

    public static final String HEALTH = "/health";

    final static String EPHEMERAL_TOKEN = UUID.randomUUID().toString();

    static final HttpString X_SWARM_HEALTH_TOKEN = HttpString.tryFromString("X_SWARM_HEALTH_TOKEN");

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

    private XnioWorker worker;
}
