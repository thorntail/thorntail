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
package org.wildfly.swarm.webservices.web;

import static org.wildfly.swarm.webservices.ws.SimpleWebserviceEndpointIface.NAMESPACE;
import static org.wildfly.swarm.webservices.ws.SimpleWebserviceEndpointIface.SERVICE_NAME;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;

import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;
import javax.xml.namespace.QName;
import javax.xml.ws.Binding;
import javax.xml.ws.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wildfly.swarm.webservices.ws.SimpleWebserviceEndpointIface;

/**
 * @author sfcoy
 */
@WebServlet("/")
public class WebserviceClientServlet extends HttpServlet {

    static final Logger logger = LoggerFactory.getLogger(WebserviceClientWithHandlerServlet.class);

    static final QName SERVICE_QNAME = new QName(NAMESPACE, SERVICE_NAME);

    @Resource(name = "java:global/ws/simplews")
    private URL wsdlLocation;

    protected SimpleWebserviceEndpointIface webServicePort;

    protected void configure(Binding wsBinding) {
    }

    @Override
    public void init() throws ServletException {
        logger.info("Using wsdlLocation = {}", wsdlLocation);
        webServicePort = Service.create(wsdlLocation, SERVICE_QNAME).getPort(SimpleWebserviceEndpointIface.class);
        configure((Binding) webServicePort);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        final String message = req.getParameter("message");
        final String echoedMessage = webServicePort.echo(message);
        logger.info("Read response: {}", echoedMessage);
        resp.setContentType(MediaType.TEXT_PLAIN);
        final PrintWriter writer = resp.getWriter();
        writer.write(echoedMessage);
        writer.flush();
        writer.close();
    }
}
