/*
 * #%L
 * Camel JMX :: Tests
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

package org.wildfly.swarm.camel.test.jmx;

import javax.management.monitor.MonitorNotification;
import javax.naming.Context;
import javax.naming.InitialContext;

import org.apache.camel.CamelContext;
import org.apache.camel.ConsumerTemplate;
import org.apache.camel.ServiceStatus;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.extension.camel.CamelAware;
import org.wildfly.extension.camel.CamelContextRegistry;
import org.wildfly.swarm.ContainerFactory;
import org.wildfly.swarm.camel.core.CamelCoreFraction;
import org.wildfly.swarm.container.Container;
import org.wildfly.swarm.spi.api.JARArchive;

/**
 * Deploys a test which monitors an JMX attribute of a route.
 *
 * @author thomas.diesler@jboss.com
 * @since 03-Jun-2013
 */
@CamelAware
@RunWith(Arquillian.class)
public class JMXIntegrationTest implements ContainerFactory {

    @Deployment
    public static JARArchive deployment() {
        final JARArchive archive = ShrinkWrap.create(JARArchive.class, "jmx-integration.jar");
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
    public void testMonitorMBeanAttribute() throws Exception {
        Context context = new InitialContext();
        CamelContextRegistry contextRegistry = (CamelContextRegistry) context.lookup("java:jboss/camel/CamelContextRegistry");

        CamelContext sysctx = contextRegistry.getCamelContext("camel-1");
        Assert.assertEquals(ServiceStatus.Started, sysctx.getStatus());
        final String routeName = sysctx.getRoutes().get(0).getId();

        CamelContext camelctx = new DefaultCamelContext();
        camelctx.addRoutes(new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("jmx:platform?format=raw&objectDomain=org.apache.camel&key.context=camel-1&key.type=routes&key.name=\"" + routeName + "\"" +
                "&monitorType=counter&observedAttribute=ExchangesTotal&granularityPeriod=500").
                to("direct:end");
            }
        });

        camelctx.start();
        try {
            ConsumerTemplate consumer = camelctx.createConsumerTemplate();
            MonitorNotification notifcation = consumer.receiveBody("direct:end", MonitorNotification.class);
            Assert.assertEquals("ExchangesTotal", notifcation.getObservedAttribute());
        } finally {
            camelctx.stop();
        }
    }
}
