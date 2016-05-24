/*
 * #%L
 * Camel CXF :: Tests
 * %%
 * Copyright (C) 2016 RedHat
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package org.wildfly.swarm.camel.test.cxf.ws;

import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.extension.camel.CamelAware;
import org.wildfly.swarm.ContainerFactory;
import org.wildfly.swarm.camel.core.CamelCoreFraction;
import org.wildfly.swarm.camel.test.cxf.ws.subA.Endpoint;
import org.wildfly.swarm.camel.test.cxf.ws.subA.EndpointImpl;
import org.wildfly.swarm.container.Container;

/**
 * Test WebService endpoint access with the cxf component.
 *
 * @author thomas.diesler@jboss.com
 * @since 11-Jun-2013
 */
@CamelAware
@RunWith(Arquillian.class)
public class CXFWSProducerIntegrationTest implements ContainerFactory {

    @Deployment
    public static WebArchive deployment() {
        final WebArchive archive = ShrinkWrap.create(WebArchive.class, "cxf-ws-producer-tests.war");
        archive.addClasses(Endpoint.class, EndpointImpl.class);
        return archive;
    }

    @Override
    public Container newContainer(String... args) throws Exception {
        return new Container().fraction(new CamelCoreFraction());
    }

    @Test
    public void testSimpleWar() throws Exception {
        QName serviceName = new QName("http://wildfly.camel.test.cxf", "EndpointService");
        Service service = Service.create(getWsdl("/"), serviceName);
        Endpoint port = service.getPort(Endpoint.class);
        Assert.assertEquals("Hello Foo", port.echo("Foo"));
    }

    @Test
    public void testCxfProducer() throws Exception {
        CamelContext camelctx = new DefaultCamelContext();
        camelctx.addRoutes(new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("direct:start").to("cxf://" + getEndpointAddress("/") + "?serviceClass=" + Endpoint.class.getName());
            }
        });

        camelctx.start();
        try {
            ProducerTemplate producer = camelctx.createProducerTemplate();
            String result = producer.requestBody("direct:start", "Kermit", String.class);
            Assert.assertEquals("Hello Kermit", result);
        } finally {
            camelctx.stop();
        }
    }

    private String getEndpointAddress(String contextPath) throws MalformedURLException {
        return "http://localhost:8080" + contextPath + "/EndpointService";
    }

    private URL getWsdl(String contextPath) throws MalformedURLException {
        return new URL(getEndpointAddress(contextPath) + "?wsdl");
    }
}
