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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

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
@WebServlet(urlPatterns = {"/system/stream"}, asyncSupported = true)
public class TopologySSEServlet extends HttpServlet {

    private Topology topology;

    private ScheduledExecutorService keepAliveExecutor;

    @Override
    public void init(ServletConfig config) throws ServletException {

        super.init(config);

        try {
            this.topology = Topology.lookup();
        } catch (NamingException e) {
            e.printStackTrace();
            throw new ServletException();
        }

        this.keepAliveExecutor = Executors.newScheduledThreadPool(2);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        resp.setContentType("text/event-stream");
        resp.setCharacterEncoding("UTF-8");

        AsyncContext asyncContext = req.startAsync();
        PrintWriter writer = resp.getWriter();

        Object writeLock = new Object();

        TopologyListener topologyListener = topo -> {
            String json = topologyToJson(req);
            synchronized (writeLock) {
                writer.write("event: topologyChange\n");
                writer.write("data: " + json);
                writer.flush();
            }
        };

        ScheduledFuture keepAlive = this.keepAliveExecutor.scheduleAtFixedRate(() -> {
                    try {
                        writer.write(":\n\n");
                        writer.flush();
                    } catch (Throwable t) {
                        TopologySSEServlet.this.topology.removeListener(topologyListener);
                        throw t;
                    }
                },
                10,
                15,
                TimeUnit.SECONDS);


        asyncContext.setTimeout(0);
        asyncContext.addListener(new AsyncListener() {
            @Override
            public void onComplete(AsyncEvent asyncEvent) throws IOException {
                TopologySSEServlet.this.topology.removeListener(topologyListener);
                keepAlive.cancel(true);
            }

            @Override
            public void onTimeout(AsyncEvent asyncEvent) throws IOException {
                TopologySSEServlet.this.topology.removeListener(topologyListener);
                keepAlive.cancel(true);
            }

            @Override
            public void onError(AsyncEvent asyncEvent) throws IOException {
                TopologySSEServlet.this.topology.removeListener(topologyListener);
                keepAlive.cancel(true);
            }

            @Override
            public void onStartAsync(AsyncEvent asyncEvent) throws IOException {
            }
        });


        this.topology.addListener(topologyListener);
        String json = topologyToJson(req);
        writer.write("event: topologyChange\n");
        writer.write("data: " + json);
        writer.flush();

    }

    protected String topologyToJson(HttpServletRequest req) {
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
                tags.add(req.isSecure() ? "https" : "http");
                while (listIter.hasNext()) {
                    Topology.Entry server = listIter.next();
                    tags.add(server.getAddress() + ":" + server.getPort());
                }
                populateEndpointAndTagsJson(json, proxyContext, tags);
            } else {
                while (listIter.hasNext()) {
                    Topology.Entry server = listIter.next();
                    String endpoint = server.getAddress() + ":" + server.getPort();
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

    protected void populateEndpointAndTagsJson(StringBuilder json, String endpoint, List<String> tags) {
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
}
