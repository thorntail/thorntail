/*
 * #%L
 * Wildfly Camel :: Testsuite
 * %%
 * Copyright (C) 2013 - 2014 RedHat
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

package org.wildfly.swarm.camel.test.jms;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.InitialContext;

import org.apache.camel.CamelContext;
import org.apache.camel.PollingConsumer;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.extension.camel.CamelAware;
import org.wildfly.swarm.ContainerFactory;
import org.wildfly.swarm.camel.full.CamelFullFraction;
import org.wildfly.swarm.config.messaging.activemq.server.JMSQueue;
import org.wildfly.swarm.container.Container;
import org.wildfly.swarm.messaging.MessagingFraction;
import org.wildfly.swarm.spi.api.JARArchive;

/**
 * Test routes that use the jms component in routes.
 *
 * @author thomas.diesler@jboss.com
 * @since 18-May-2013
 */
@CamelAware
@RunWith(Arquillian.class)
public class JMSIntegrationTest implements ContainerFactory {

    static final String QUEUE_NAME = "camel-jms-queue";
    static final String QUEUE_JNDI_NAME = "java:/" + QUEUE_NAME;

    @Deployment
    public static JARArchive deployment() {
        JARArchive archive = ShrinkWrap.create(JARArchive.class, "camel-jms-tests.jar");
        archive.addModule("org.wildfly.swarm.messaging");
        return archive;
    }

    @Override
    public Container newContainer(String... args) throws Exception {
        Container container = new Container().fraction(new CamelFullFraction());
        container.fraction(MessagingFraction.createDefaultFraction()
                .defaultServer((s) -> {
                    s.jmsQueue(new JMSQueue<>(QUEUE_NAME).entry(QUEUE_JNDI_NAME));
                }));
        return container;
    }

    @Test
    public void testMessageConsumerRoute() throws Exception {

        CamelContext camelctx = new DefaultCamelContext();
        camelctx.addRoutes(new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("jms:queue:" + QUEUE_NAME + "?connectionFactory=ConnectionFactory").
                transform(body().prepend("Hello ")).to("direct:end");
            }
        });

        camelctx.start();

        PollingConsumer consumer = camelctx.getEndpoint("direct:end").createPollingConsumer();
        consumer.start();

        try {
            // Send a message to the queue
            InitialContext initialctx = new InitialContext();
            ConnectionFactory cfactory = (ConnectionFactory) initialctx.lookup("java:/ConnectionFactory");
            Connection connection = cfactory.createConnection();
            try {
                sendMessage(connection, QUEUE_JNDI_NAME, "Kermit");
                String result = consumer.receive().getIn().getBody(String.class);
                Assert.assertEquals("Hello Kermit", result);
            } finally {
                connection.close();
            }
        } finally {
            camelctx.stop();
        }
    }

    @Test
    public void testMessageProviderRoute() throws Exception {

        CamelContext camelctx = new DefaultCamelContext();
        camelctx.addRoutes(new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("direct:start").
                transform(body().prepend("Hello ")).
                to("jms:queue:" + QUEUE_NAME + "?connectionFactory=ConnectionFactory");
            }
        });

        final List<String> result = new ArrayList<>();
        final CountDownLatch latch = new CountDownLatch(1);

        camelctx.start();
        try {
            // Get the message from the queue
            InitialContext initialctx = new InitialContext();
            ConnectionFactory cfactory = (ConnectionFactory) initialctx.lookup("java:/ConnectionFactory");
            Connection connection = cfactory.createConnection();
            try {
                receiveMessage(connection, QUEUE_JNDI_NAME, new MessageListener() {
                    @Override
                    public void onMessage(Message message) {
                        TextMessage text = (TextMessage) message;
                        try {
                            result.add(text.getText());
                        } catch (JMSException ex) {
                            result.add(ex.getMessage());
                        }
                        latch.countDown();
                    }
                });

                ProducerTemplate producer = camelctx.createProducerTemplate();
                producer.asyncSendBody("direct:start", "Kermit");

                Assert.assertTrue(latch.await(10, TimeUnit.SECONDS));
                Assert.assertEquals("One message", 1, result.size());
                Assert.assertEquals("Hello Kermit", result.get(0));
            } finally {
                connection.close();
            }
        } finally {
            camelctx.stop();
        }
    }

    private void sendMessage(Connection connection, String jndiName, String message) throws Exception {
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Destination destination = (Destination) new InitialContext().lookup(jndiName);
        MessageProducer producer = session.createProducer(destination);
        TextMessage msg = session.createTextMessage(message);
        producer.send(msg);
        connection.start();
    }

    private Session receiveMessage(Connection connection, String jndiName, MessageListener listener) throws Exception {
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Destination destination = (Destination) new InitialContext().lookup(jndiName);
        MessageConsumer consumer = session.createConsumer(destination);
        consumer.setMessageListener(listener);
        connection.start();
        return session;
    }
}
