/*
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

import org.wildfly.swarm.config.messaging.activemq.server.BroadcastGroup;
import org.wildfly.swarm.config.messaging.activemq.server.ClusterConnection;
import org.wildfly.swarm.config.messaging.activemq.server.ConnectionFactory;
import org.wildfly.swarm.config.messaging.activemq.server.DiscoveryGroup;
import org.wildfly.swarm.config.messaging.activemq.server.HTTPAcceptor;
import org.wildfly.swarm.config.messaging.activemq.server.HTTPConnector;
import org.wildfly.swarm.config.messaging.activemq.server.JMSQueueConsumer;
import org.wildfly.swarm.config.messaging.activemq.server.JMSTopicConsumer;
import org.wildfly.swarm.config.messaging.activemq.server.PooledConnectionFactory;

/**
 * @author Bob McWhirter
 */
@SuppressWarnings("unused")
public class EnhancedServer extends org.wildfly.swarm.config.messaging.activemq.Server<EnhancedServer> {
    public EnhancedServer(String key) {
        super(key);
    }

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

    public EnhancedServer enableClustering() {
        enableHTTPConnections();

        // add the jboss.messaging.cluster.password property to set the ActiveMQ cluster password.
        clusterPassword("${jboss.messaging.cluster.password:CHANGE ME!!}");

        discoveryGroup(new DiscoveryGroup("activemq-discovery")
                .jgroupsChannel("activemq-jgroups-cluster"));
        broadcastGroup(new BroadcastGroup("activemq-broadcast")
                .jgroupsChannel("activemq-jgroups-cluster")
                .connectors("http-connector"));
        clusterConnection(new ClusterConnection("activemq-cluster")
                .clusterConnectionAddress("jms")
                .connectorName("http-connector")
                .discoveryGroup("activemq-discovery"));

        return this;
    }

    public EnhancedServer enableRemote() {
        enableHTTPConnections();

        connectionFactory(new ConnectionFactory("RemoteConnectionFactory")
                                  .connectors(Collections.singletonList("http-connector"))
                                  .entries("java:/RemoteConnectionFactory", "java:jboss/exported/jms/RemoteConnectionFactory"));
        return this;
    }

    private EnhancedServer enableHTTPConnections() {
        if (this.subresources().acceptor(("http-acceptor")) != null) {
            return this;
        }
        httpAcceptor(new HTTPAcceptor("http-acceptor")
                .httpListener("default"));
        httpConnector(new HTTPConnector("http-connector")
                .socketBinding("http")
                .endpoint("http-acceptor"));
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

    public EnhancedServer remoteJmsQueue(String childKey) {
        remoteJmsQueue(childKey, null);
        return this;
    }

    @SuppressWarnings("unchecked")
    public EnhancedServer remoteJmsQueue(String childKey, JMSQueueConsumer config) {
        return super.jmsQueue(childKey, (q) -> {
            if (config != null) {
                config.accept(q);
            }
            if (q.entries() == null || q.entries().isEmpty()) {
                q.entry("java:/jboss/exported/jms/queue/" + childKey);
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

    public EnhancedServer remoteJmsTopic(String childKey) {
        remoteJmsTopic(childKey, null);
        return this;
    }

    @SuppressWarnings("unchecked")
    public EnhancedServer remoteJmsTopic(String childKey, JMSTopicConsumer config) {
        return super.jmsTopic(childKey, (t) -> {
            if (config != null) {
                config.accept(t);
            }
            if (t.entries() == null || t.entries().isEmpty()) {
                t.entry("java:/jboss/exported/jms/topic/" + childKey);
                t.entry("java:/jms/topic/" + childKey);
            }
        });
    }

    private static final AtomicInteger COUNTER = new AtomicInteger();
}
