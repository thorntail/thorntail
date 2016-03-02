/*
 * #%L
 * Wildfly Camel :: Testsuite
 * %%
 * Copyright (C) 2013 - 2015 RedHat
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
package org.wildfly.camel.swarm.tests.jaxrs;

import java.net.MalformedURLException;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
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
import org.wildfly.camel.swarm.servlet.CamelServletFraction;
import org.wildfly.camel.swarm.tests.jaxrs.subA.GreetingService;
import org.wildfly.camel.swarm.tests.jaxrs.subA.RestApplication;
import org.wildfly.extension.camel.CamelAware;
import org.wildfly.swarm.ContainerFactory;
import org.wildfly.swarm.container.Container;

@CamelAware
@RunWith(Arquillian.class)
public class JAXRSProducerTest implements ContainerFactory {

    static final String JAXRS_ENDPOINT_WAR = "jaxrs-endpoint.war";

    @Deployment(testable = false)
    public static WebArchive swarmDeployment() {
        final WebArchive archive = ShrinkWrap.create(WebArchive.class, JAXRS_ENDPOINT_WAR);
        archive.addPackage(RestApplication.class.getPackage());
        return archive;
    }

    @Override
    public Container newContainer(String... args) throws Exception {
        return new Container().fraction(new CamelServletFraction());
    }

    @Test
    public void testJaxrsProducer() throws Exception {
        CamelContext camelctx = new DefaultCamelContext();
        camelctx.addRoutes(new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("direct:start")
                .setHeader(Exchange.HTTP_METHOD, constant("GET")).
                to("cxfrs://" + getEndpointAddress("/") + "?resourceClasses=" + GreetingService.class.getName());
            }
        });

        camelctx.start();
        try {
            ProducerTemplate producer = camelctx.createProducerTemplate();
            String result = producer.requestBodyAndHeader("direct:start", "mybody", "name", "Kermit", String.class);
            Assert.assertEquals("Hello Kermit", result);
        } finally {
            camelctx.stop();
        }
    }

    private String getEndpointAddress(String contextPath) throws MalformedURLException {
        return "http://localhost:8080" + contextPath + "/rest/greet/hello/Kermit";
    }
}
