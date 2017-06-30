/*
 * #%L
 * Camel JMS :: Tests
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

package org.wildfly.swarm.camel.test.jms;

import org.wildfly.swarm.Swarm;
import org.wildfly.swarm.camel.core.CamelFraction;
import org.wildfly.swarm.config.messaging.activemq.server.JMSQueue;
import org.wildfly.swarm.messaging.MessagingFraction;

/**
 * Test routes that use the jms component in routes.
 *
 * @author thomas.diesler@jboss.com
 * @since 18-May-2013
 */
public class Main {

    protected Main() {
    }

    static final String QUEUE_NAME = "camel-jms-queue";

    static final String QUEUE_JNDI_NAME = "java:/" + QUEUE_NAME;

    private static Swarm container;

    public static void main(String... args) throws Exception {
        System.err.println("RUNNING MAIN!");
        container = new Swarm().fraction(new CamelFraction());
        container.fraction(MessagingFraction.createDefaultFraction()
                                   .defaultServer((s) -> {
                                       s.jmsQueue(new JMSQueue<>(QUEUE_NAME).entry(QUEUE_JNDI_NAME));
                                   }));

        container.start().deploy();
    }

    public static void stopMain() throws Exception {
        container.stop();
    }
}
