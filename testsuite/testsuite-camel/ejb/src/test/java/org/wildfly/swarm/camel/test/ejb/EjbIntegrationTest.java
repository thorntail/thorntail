/*
 * #%L
 * Camel EJB :: Tests
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

package org.wildfly.swarm.camel.test.ejb;

import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.extension.camel.CamelAware;
import org.wildfly.swarm.arquillian.DefaultDeployment;

import static org.wildfly.swarm.arquillian.DefaultDeployment.Type.JAR;

@CamelAware
@RunWith(Arquillian.class)
@DefaultDeployment(type=JAR)
public class EjbIntegrationTest {

    @Test
    public void testStatelessSessionBean() throws Exception {

        CamelContext camelctx = new DefaultCamelContext();
        camelctx.addRoutes(new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("direct:start").to("ejb:java:module/HelloBean");
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
}
