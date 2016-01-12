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
package org.wildfly.swarm.ribbon.webapp.runtime;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.naming.InitialContext;
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

import org.wildfly.swarm.netflix.ribbon.RibbonExternalAddressMapper;
import org.wildfly.swarm.netflix.ribbon.RibbonServer;
import org.wildfly.swarm.netflix.ribbon.RibbonTopology;
import org.wildfly.swarm.netflix.ribbon.RibbonTopologyListener;


/**
 * @author Bob McWhirter
 */
@WebServlet(urlPatterns = {"/system/stream"}, asyncSupported = true)
public class RibbonToTheCurbSSEServlet extends HttpServlet {

    private RibbonTopology topology;
    private RibbonExternalAddressMapper externalAddressMapper;

    @Override
    public void init(ServletConfig config) throws ServletException {

        super.init(config);

        try {
            InitialContext context = new InitialContext();
            this.topology = (RibbonTopology) context.lookup("jboss/ribbon/cluster");
        } catch (NamingException e) {
            e.printStackTrace();
            throw new ServletException();
        }
        try {
            Class clazz = Class.forName(config.getServletContext().getInitParameter("externalAddressMapper"));
            this.externalAddressMapper = (RibbonExternalAddressMapper) clazz.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
            throw new ServletException(e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        resp.setContentType("text/event-stream");
        resp.setCharacterEncoding("UTF-8");

        AsyncContext asyncContext = req.startAsync();
        PrintWriter writer = resp.getWriter();

        Object writeLock = new Object();

        RibbonTopologyListener topologyListener = new RibbonTopologyListener() {
            @Override
            public void onChange(RibbonTopology topology) {
                String json = topologyToJson(req.getServerPort());
                synchronized (writeLock) {
                    writer.write("event: topologyChange\n");
                    writer.write("data: " + json);
                    writer.flush();
                }
            }
        };

        Thread keepAlive = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(15_000);
                    synchronized (writeLock) {
                        writer.write(":\n\n");
                        writer.flush();
                    }
                } catch (InterruptedException e) {
                    break;
                }
            }
        });

        asyncContext.setTimeout(0);
        asyncContext.addListener(new AsyncListener() {
            @Override
            public void onComplete(AsyncEvent asyncEvent) throws IOException {
                RibbonToTheCurbSSEServlet.this.topology.removeListener(topologyListener);
                keepAlive.interrupt();
            }

            @Override
            public void onTimeout(AsyncEvent asyncEvent) throws IOException {
                RibbonToTheCurbSSEServlet.this.topology.removeListener(topologyListener);
                keepAlive.interrupt();
            }

            @Override
            public void onError(AsyncEvent asyncEvent) throws IOException {
                RibbonToTheCurbSSEServlet.this.topology.removeListener(topologyListener);
                keepAlive.interrupt();
            }

            @Override
            public void onStartAsync(AsyncEvent asyncEvent) throws IOException {
            }
        });


        this.topology.addListener(topologyListener);
        String json = topologyToJson(req.getServerPort());
        writer.write("event: topologyChange\n");
        writer.write("data: " + json);
        writer.flush();

        keepAlive.start();

    }

    protected String topologyToJson(int externalPort) {
        StringBuilder json = new StringBuilder();

        json.append("{");

        Map<String, List<RibbonServer>> map = this.topology.asMap();

        Set<String> keys = map.keySet();
        Iterator<String> keyIter = keys.iterator();

        while (keyIter.hasNext()) {
            String key = keyIter.next();
            json.append("  ").append('"').append(key).append('"').append(": [");
            List<RibbonServer> list = map.get(key);
            Iterator<RibbonServer> listIter = list.iterator();
            while (listIter.hasNext()) {
                RibbonServer server = listIter.next();
                server = this.externalAddressMapper.toExternal(server, externalPort);
                json.append("    ").append('"').append(server).append('"');
                if (listIter.hasNext()) {
                    json.append(", ");
                }
                json.append("");
            }

            json.append("  ]");
            if (keyIter.hasNext()) {
                json.append(",");
            }
            json.append("");
        }

        json.append("}\n\n");

        return json.toString();
    }
}
