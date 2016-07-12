/*
 * #%L
 * Camel Core :: Tests
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
package org.wildfly.swarm.camel.core;

import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.ServiceStatus;
import org.apache.camel.builder.RouteBuilder;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.gravia.runtime.ServiceLocator;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.extension.camel.CamelAware;
import org.wildfly.extension.camel.CamelContextRegistry;
import org.wildfly.swarm.ContainerFactory;
import org.wildfly.swarm.container.Container;
import org.wildfly.swarm.spi.api.JARArchive;


/**
 * Verifiy usage of a system camel context
 * 
 * @author thomas.diesler@jboss.com
 * @since 09-Mar-2016
 */
@CamelAware
@RunWith(Arquillian.class)
public class SystemContextTransformTest implements ContainerFactory {

    @Deployment
    public static JARArchive deployment() {
        JARArchive archive = ShrinkWrap.create(JARArchive.class, "system-context-tests.jar");
        archive.addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
        return archive;
    }

    @Override
    public Container newContainer(String... args) throws Exception {
        return new Container().fraction(new CamelCoreFraction().addRouteBuilder(new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("direct:start")
                .transform(simple("Hello ${body}"));
            }
        }));
    }

    @Test
    public void testSimpleTransform() throws Exception {

        CamelContextRegistry contextRegistry = ServiceLocator.getRequiredService(CamelContextRegistry.class);
        CamelContext camelctx = contextRegistry.getCamelContext("camel-1");
        Assert.assertEquals(CamelContextRegistry.class.getClassLoader(), camelctx.getApplicationContextClassLoader());
        Assert.assertEquals(ServiceStatus.Started, camelctx.getStatus());

        ProducerTemplate producer = camelctx.createProducerTemplate();
        String result = producer.requestBody("direct:start", "Kermit", String.class);
        Assert.assertEquals("Hello Kermit", result);
    }
}
