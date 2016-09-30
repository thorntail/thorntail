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
package org.wildfly.swarm.webservices;

import static java.net.URLEncoder.encode;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.wildfly.swarm.webservices.ws.SimpleWebserviceEndpointIface.NAMESPACE;
import static org.wildfly.swarm.webservices.ws.SimpleWebserviceEndpointIface.SERVICE_NAME;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.CharBuffer;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;
import javax.xml.ws.handler.Handler;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.swarm.ContainerFactory;
import org.wildfly.swarm.config.naming.Binding;
import org.wildfly.swarm.container.Container;
import org.wildfly.swarm.naming.NamingFraction;
import org.wildfly.swarm.undertow.WARArchive;
import org.wildfly.swarm.webservices.support.SimpleWSServer;
import org.wildfly.swarm.webservices.web.WebserviceClientServlet;
import org.wildfly.swarm.webservices.web.WebserviceClientWithHandlerServlet;
import org.wildfly.swarm.webservices.ws.SampleSoapHandler;
import org.wildfly.swarm.webservices.ws.SimpleWebserviceEndpointIface;
import org.wildfly.swarm.webservices.ws.SimpleWebserviceEndpointImpl;

/**
 * @author sfcoy
 */
@RunWith(Arquillian.class)
public class JAXWSClientTest  implements ContainerFactory {

    private static final String MESSAGE_TO_ECHO = "Lorem ipsum dolor sit amet";

    private static SimpleWSServer wsServer;

    public JAXWSClientTest() throws IOException {
        wsServer = new SimpleWSServer();
        wsServer.buildAndStartWebService();
    }

    @Deployment(testable = false)
    public static Archive createDeployment() throws IOException {
        System.out.println("Creating deployment");
        final WARArchive warArchive = ShrinkWrap.create(WARArchive.class, "myapp.war")
                .addClass(WebserviceClientServlet.class)
                .addClass(WebserviceClientWithHandlerServlet.class)
                .addClass(SampleSoapHandler.class)
                .addClass(SimpleWSServer.class)
                .addClass(SimpleWebserviceEndpointImpl.class)
                .addClass(SimpleWebserviceEndpointIface.class);
        return warArchive;
    }

    public Container newContainer(String... args) throws Exception {
        System.out.println("Building container");
        return new Container(false)
                .fraction(WebServicesFraction.createDefaultFraction())
                .fraction(buildNamingFraction());
    }


    @Test
    @RunAsClient
    public void testServletAsWebServiceClient() throws Exception {
        final URL webappURL = new URL("http://localhost:8080/myapp?message=" + encode(MESSAGE_TO_ECHO, UTF_8.name()));
        final String response = readResponse(webappURL);

        assertThat(response, is(MESSAGE_TO_ECHO));
    }

    @Test
    @RunAsClient
    public void testServletAsWebServiceClientWithSoapHandler() throws Exception {
        final URL webappURL = new URL("http://localhost:8080/myapp/handled?message=" + encode(MESSAGE_TO_ECHO, UTF_8.name()));
        final String response = readResponse(webappURL);

        assertThat(response, is(MESSAGE_TO_ECHO));
    }

    private String readResponse(URL webappURL) throws IOException {
        final InputStream webInputStream = webappURL.openStream();
        try (final BufferedReader responseReader = new BufferedReader(new InputStreamReader(webInputStream))) {
            final CharBuffer inputBuffer = CharBuffer.allocate(1000);
            inputBuffer.mark();
            while (responseReader.read(inputBuffer) > 0) {
            }
            final int responseLength = inputBuffer.position();
            inputBuffer.reset();
            final String response = inputBuffer.subSequence(0, responseLength).toString();
            System.out.println("Response length is " + response.length());
            return response;
        }
    }

    private NamingFraction buildNamingFraction() throws MalformedURLException {
        return new NamingFraction().binding(createServerUrlBinding());
    }

    private Binding createServerUrlBinding() {
        return new Binding("java:global/ws/simplews")
                .bindingType(Binding.BindingType.SIMPLE)
                .type(URL.class.getCanonicalName())
                .value(wsServer.getServerURL().toExternalForm() + "?wsdl");
    }

}
