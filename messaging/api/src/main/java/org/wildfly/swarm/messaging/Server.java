/**
 * Copyright 2015 Red Hat, Inc, and individual contributors.
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

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

import org.wildfly.swarm.config.messaging_activemq.server.ConnectionFactory;
import org.wildfly.swarm.config.messaging_activemq.server.JMSQueue;
import org.wildfly.swarm.config.messaging_activemq.server.JMSQueueConfigurator;
import org.wildfly.swarm.config.messaging_activemq.server.JMSTopic;
import org.wildfly.swarm.config.messaging_activemq.server.JMSTopicConfigurator;
import org.wildfly.swarm.config.messaging_activemq.server.PooledConnectionFactory;

/**
 * @author Bob McWhirter
 */
public class Server extends org.wildfly.swarm.config.messaging_activemq.Server<Server> {
    private static final AtomicInteger COUNTER = new AtomicInteger();

    public Server(String key) {
        super(key);
    }

    public Server enableInVm() {
        int serverId = COUNTER.getAndIncrement();

        inVmConnector( "in-vm", (c)->{
            c.serverId( serverId );
        });

        inVmAcceptor( "in-vm", (a)->{
            a.serverId(serverId);
        });

        connectionFactory(new ConnectionFactory("InVmConnectionFactory")
                .connectors(Arrays.asList("in-vm"))
                .entries(Arrays.asList("java:/ConnectionFactory")));

        pooledConnectionFactory(new PooledConnectionFactory("activemq-ra")
                .entries(Arrays.asList("java:jboss/DefaultJMSConnectionFactory"))
                .connectors(Arrays.asList("in-vm"))
                .transaction("xa"));

        return this;
    }

    @Override
    public Server jmsQueue(String childKey, JMSQueueConfigurator config) {
        JMSQueue queue = new JMSQueue(childKey);
        if ( config != null ) {
            config.configure(queue);
        }
        System.err.println( "queeu entries: " + queue.entries() );
        if ( queue.entries() == null ) {
            queue.entries( Arrays.asList( "java:/jms/queue/" + childKey ));
        }
        jmsQueue(queue);
        return this;
    }

    @Override
    public Server jmsTopic(String childKey, JMSTopicConfigurator config) {
        JMSTopic topic = new JMSTopic(childKey);
        if( config != null ) {
            config.configure(topic);
        }
        System.err.println( "topic entries: " + topic.entries() );
        if ( topic.entries() == null ) {
            topic.entries( Arrays.asList( "java:/jms/topic/" + childKey ));
        }
        jmsTopic(topic);
        return this;
    }
}
