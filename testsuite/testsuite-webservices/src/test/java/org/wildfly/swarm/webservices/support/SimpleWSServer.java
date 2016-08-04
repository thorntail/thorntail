/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2016, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.wildfly.swarm.webservices.support;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.URL;

import javax.xml.ws.Endpoint;

import org.wildfly.swarm.webservices.ws.SimpleWebserviceEndpointImpl;

/**
 * @author sfcoy
 */
public class SimpleWSServer {
    private URL serverURL;

    private Endpoint wsServer;

    private static int findFreeTcpPort() throws IOException {
        try (ServerSocket socket = new ServerSocket(0)) {
            socket.setReuseAddress(true);
            return socket.getLocalPort();
        }
    }

    public URL getServerURL() {
        return serverURL;
    }

    public void buildAndStartWebService() throws IOException {
        serverURL = new URL("http", "localhost", findFreeTcpPort(), "/hellows");
        wsServer = Endpoint.publish(serverURL.toExternalForm(), new SimpleWebserviceEndpointImpl());
        System.out.println("Web service running at " + serverURL);
    }

    public void shutdown() {
        if (wsServer != null)
            wsServer.stop();
    }

}
