/**
 * Copyright 2015-2016 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wildfly.swarm.messaging;

import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;

import org.wildfly.swarm.config.messaging.activemq.server.ConnectionFactory;
import org.wildfly.swarm.config.messaging.activemq.server.JMSQueueConsumer;
import org.wildfly.swarm.config.messaging.activemq.server.JMSTopicConsumer;
import org.wildfly.swarm.config.messaging.activemq.server.PooledConnectionFactory;

/**
 * @author Bob McWhirter
 */
public class EnhancedServer extends org.wildfly.swarm.config.messaging.activemq.Server<EnhancedServer> {
    public EnhancedServer(String key) {
        super(key);
    }

    @SuppressWarnings("unchecked")
    public EnhancedServer enableInVm() {
        int serverId = COUNTER.getAndIncrement();

        inVmConnector("in-vm", (c) -> c.serverId(serverId));

        inVmAcceptor("in-vm", (a) -> a.serverId(serverId));

        connectionFactory(new ConnectionFactory("InVmConnectionFactory")
                                  .connectors(Collections.singletonList("in-vm"))
                                  .entries(Collections.singletonList("java:/ConnectionFactory")));

        pooledConnectionFactory(new PooledConnectionFactory("activemq-ra")
                                        .entries(Collections.singletonList("java:jboss/DefaultJMSConnectionFactory"))
                                        .connectors(Collections.singletonList("in-vm"))
                                        .transaction("xa"));

        return this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public EnhancedServer jmsQueue(String childKey, JMSQueueConsumer config) {
        return super.jmsQueue(childKey, (q) -> {
            if (config != null) {
                config.accept(q);
            }
            if (q.entries() == null || q.entries().isEmpty()) {
                q.entry("java:/jms/queue/" + childKey);
            }
        });
    }

    @SuppressWarnings("unchecked")
    @Override
    public EnhancedServer jmsTopic(String childKey, JMSTopicConsumer config) {
        return super.jmsTopic(childKey, (t) -> {
            if (config != null) {
                config.accept(t);
            }
            if (t.entries() == null || t.entries().isEmpty()) {
                t.entry("java:/jms/topic/" + childKey);
            }
        });
    }

    private static final AtomicInteger COUNTER = new AtomicInteger();
}
