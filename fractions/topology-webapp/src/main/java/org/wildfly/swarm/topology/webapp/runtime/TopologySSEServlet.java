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
package org.wildfly.swarm.topology.webapp.runtime;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.enterprise.inject.Vetoed;
import javax.naming.NamingException;
import javax.servlet.AsyncContext;
import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.wildfly.swarm.topology.Topology;
import org.wildfly.swarm.topology.TopologyListener;


/**
 * @author Bob McWhirter
 */
@Vetoed
@WebServlet(urlPatterns = {"/system/stream"}, asyncSupported = true)
public class TopologySSEServlet extends HttpServlet {

    @Override
    public void init(ServletConfig config) throws ServletException {

        super.init(config);

        try {
            this.topology = Topology.lookup();
        } catch (NamingException e) {
            throw new ServletException(e);
        }

        this.keepAliveExecutor = Executors.newScheduledThreadPool(2);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        resp.setContentType("text/event-stream");
        resp.setCharacterEncoding("UTF-8");

        AsyncContext asyncContext = req.startAsync();
        PrintWriter writer = resp.getWriter();

        TopologyListener topologyListener = new SSETopologyListener(writer, req.isSecure());

        ScheduledFuture keepAlive = this.keepAliveExecutor.scheduleAtFixedRate(
                new KeepAliveRunnable(writer, topologyListener),
                10,
                15,
                TimeUnit.SECONDS);
        asyncContext.setTimeout(0);
        asyncContext.addListener(new TopologyAsyncListener(topology, topologyListener, keepAlive));


        this.topology.addListener(topologyListener);
        String json = topologyToJson(req.isSecure());
        writer.write("event: topologyChange\n");
        writer.write("data: " + json);
        writer.flush();

    }

    private String topologyToJson(boolean secure) {
        StringBuilder json = new StringBuilder();

        json.append("{");

        Map<String, List<Topology.Entry>> map = this.topology.asMap();

        Set<String> keys = map.keySet();
        Iterator<String> keyIter = keys.iterator();

        while (keyIter.hasNext()) {
            String key = keyIter.next();
            json.append("  ").append('"').append(key).append('"').append(": [");
            List<Topology.Entry> list = map.get(key);
            Iterator<Topology.Entry> listIter = list.iterator();
            String proxyContext = getServletContext().getInitParameter(key + "-proxy");
            if (proxyContext != null) {
                List<String> tags = new ArrayList<>();
                tags.add(secure ? "https" : "http");
                while (listIter.hasNext()) {
                    Topology.Entry server = listIter.next();
                    tags.add(server.getAddress() + ":" + server.getPort());
                }
                populateEndpointAndTagsJson(json, proxyContext, tags);
            } else {
                while (listIter.hasNext()) {
                    Topology.Entry server = listIter.next();

                    boolean invalidServerAddress = false;
                    try {
                        //noinspection ResultOfMethodCallIgnored
                        InetAddress.getByName(server.getAddress());
                    } catch (UnknownHostException e) {
                        invalidServerAddress = true;
                    }

                    String endpoint = (!invalidServerAddress ? (server.getTags().contains("https") ? "https" : "http") + "://" : "")
                            + server.getAddress() + ":" + server.getPort();
                    populateEndpointAndTagsJson(json, endpoint, server.getTags());
                    if (listIter.hasNext()) {
                        json.append(",");
                    }
                }
            }
            json.append("]");
            if (keyIter.hasNext()) {
                json.append(",");
            }
        }

        json.append("}\n\n");
        return json.toString();
    }

    private void populateEndpointAndTagsJson(StringBuilder json, String endpoint, List<String> tags) {
        json.append("{");
        json.append("\"endpoint\": \"").append(endpoint).append("\",");
        json.append("\"tags\":[");
        Iterator<String> tagIter = tags.iterator();
        while (tagIter.hasNext()) {
            String tag = tagIter.next();
            json.append("\"").append(tag).append("\"");
            if (tagIter.hasNext()) {
                json.append(",");
            }
        }
        json.append("]");
        json.append("}");
    }

    private Topology topology;

    private ScheduledExecutorService keepAliveExecutor;

    private class KeepAliveRunnable implements Runnable {
        private final PrintWriter writer;

        private final TopologyListener topologyListener;

        public KeepAliveRunnable(PrintWriter writer, TopologyListener topologyListener) {
            this.writer = writer;
            this.topologyListener = topologyListener;
        }

        @Override
        public void run() {
            try {
                writer.write(":\n\n");
                writer.flush();
            } catch (Throwable t) {
                TopologySSEServlet.this.topology.removeListener(topologyListener);
                throw t;
            }

        }
    }

    public static class TopologyAsyncListener implements AsyncListener {
        private final ScheduledFuture keepAlive;

        private final TopologyListener topologyListener;

        private final Topology topology;

        public TopologyAsyncListener() {
            // Do Nothing
            this.topology = null;
            this.topologyListener = null;
            this.keepAlive = null;
        }

        public TopologyAsyncListener(Topology topology, TopologyListener topologyListener, ScheduledFuture scheduledFuture) {
            this.topology = topology;
            this.topologyListener = topologyListener;
            this.keepAlive = scheduledFuture;
        }

        @Override
        public void onComplete(AsyncEvent asyncEvent) throws IOException {
            if (topology != null) {
                topology.removeListener(topologyListener);
                keepAlive.cancel(true);
            }
        }

        @Override
        public void onTimeout(AsyncEvent asyncEvent) throws IOException {
            if (topology != null) {
                topology.removeListener(topologyListener);
                keepAlive.cancel(true);
            }
        }

        @Override
        public void onError(AsyncEvent asyncEvent) throws IOException {
            if (topology != null) {
                topology.removeListener(topologyListener);
                keepAlive.cancel(true);
            }
        }

        @Override
        public void onStartAsync(AsyncEvent asyncEvent) throws IOException {
        }
    }

    private class SSETopologyListener implements TopologyListener {
        Object writeLock = new Object();

        final PrintWriter writer;

        final boolean secure;

        private SSETopologyListener(PrintWriter writer, boolean secure) {
            this.writer = writer;
            this.secure = secure;
        }

        @Override
        public void onChange(Topology topology) {
            String json = topologyToJson(secure);
            synchronized (writeLock) {
                writer.write("event: topologyChange\n");
                writer.write("data: " + json);
                writer.flush();
            }
        }
    }
}
