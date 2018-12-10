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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.wildfly.swarm.config.messaging.activemq.DiscoveryGroup;
import org.wildfly.swarm.config.messaging.activemq.HTTPConnector;
import org.wildfly.swarm.config.messaging.activemq.PooledConnectionFactory;
import org.wildfly.swarm.config.messaging.activemq.server.BroadcastGroup;
import org.wildfly.swarm.config.messaging.activemq.server.ClusterConnection;
import org.wildfly.swarm.config.messaging.activemq.server.ConnectionFactory;
import org.wildfly.swarm.config.messaging.activemq.server.HTTPAcceptor;
import org.wildfly.swarm.config.messaging.activemq.server.JMSQueueConsumer;
import org.wildfly.swarm.config.messaging.activemq.server.JMSTopicConsumer;

/**
 * @author Bob McWhirter
 */
@SuppressWarnings("unused")
public class EnhancedServer extends org.wildfly.swarm.config.messaging.activemq.Server<EnhancedServer> {

    private static final String IN_VM = "in-vm";

    private static final String HTTP_CONNECTOR = "http-connector";

    public EnhancedServer(String key) {
        super(key);
    }

    public EnhancedServer enableInVm() {
        int serverId = COUNTER.getAndIncrement();

        securitySetting("#", security -> {
            security.role("guest", guest -> {
                guest.send(true);
                guest.consume(true);
                guest.createNonDurableQueue(true);
                guest.deleteNonDurableQueue(true);
            });
        });

        addressSetting("#", address -> {
            address.deadLetterAddress("jms.queue.DLQ");
            address.expiryAddress("jms.queue.ExpiryQueue");
            address.maxSizeBytes(10485760L);
            address.pageSizeBytes(2097152L);
            address.messageCounterHistoryDayLimit(10);
        });

        inVmConnector(IN_VM, (c) -> c.serverId(serverId).param("buffer-pooling", "false"));

        inVmAcceptor(IN_VM, (a) -> a.serverId(serverId).param("buffer-pooling", "false"));

        jmsQueue("ExpiryQueue", queue -> {
            queue.entry("java:/jms/queue/ExpiryQueue");
        });

        jmsQueue("DLQ", queue -> {
            queue.entry("java:/jms/queue/DLQ");
        });

        connectionFactory(new ConnectionFactory("InVmConnectionFactory")
                                  .connector(IN_VM)
                                  .entry("java:/ConnectionFactory"));

        pooledConnectionFactory(new PooledConnectionFactory("activemq-ra")
                                        .entry("java:jboss/DefaultJMSConnectionFactory")
                                        .entry("java:/JmsXA")
                                        .connector(IN_VM)
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
                               .connectors(HTTP_CONNECTOR));
        clusterConnection(new ClusterConnection("activemq-cluster")
                                  .clusterConnectionAddress("jms")
                                  .connectorName(HTTP_CONNECTOR)
                                  .discoveryGroup("activemq-discovery"));

        return this;
    }

    /**
     * Setup a remote connection to a remote message broker.
     *
     * @param connection The connection defailts.
     * @return This server.
     */
    public EnhancedServer remoteConnection(RemoteConnection connection) {
        return remoteConnection(() -> connection);
    }

    /**
     * Setup a default remote connection to a remote message broker.
     *
     * <p>By default, it sets up a connection named <code>remote-mq</code>,
     * connecting to <code>localhost</code> at port <code>61616</code>.
     * The connection factory is named <code>java:/jms/remote-mq</code>.</p>
     *
     * @return This server.
     */
    public EnhancedServer remoteConnection() {
        return remoteConnection(MessagingProperties.DEFAULT_REMOTE_MQ_NAME);
    }

    /**
     * Setup a default named remote connection to a remote message broker.
     *
     * <p>By default, it sets up a connection
     * connecting to <code>localhost</code> at port <code>61616</code>.
     * The connection factory is named <code>java:/jms/<b>name</b></code>.</p>
     *
     * @return This server.
     */
    public EnhancedServer remoteConnection(String name) {
        return remoteConnection(name, (config) -> {
        });
    }

    /**
     * Setup a named remote connection to a remote message broker.
     *
     * @param name   The name of the connection.
     * @param config The configuration.
     * @return This server.
     */
    public EnhancedServer remoteConnection(String name, RemoteConnection.Consumer config) {
        return remoteConnection(() -> {
            RemoteConnection connection = new RemoteConnection(name);
            config.accept(connection);
            return connection;
        });
    }


    /**
     * Setup a remote connection to a remote message broker.
     *
     * @param supplier The supplier of the configuration.
     * @return This server.
     */
    public EnhancedServer remoteConnection(RemoteConnection.Supplier supplier) {
        RemoteConnection connection = supplier.get();


        this.remoteConnections.add(connection);

        return this;
    }


    public EnhancedServer enableRemote() {
        enableHTTPConnections();

        connectionFactory(new ConnectionFactory<>("RemoteConnectionFactory")
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
        httpAcceptor(new HTTPAcceptor("http-acceptor-throughput")
                             .httpListener("default")
                             .param("batch-delay", "50")
                             .param("direct-deliver", "false"));
        httpConnector(new HTTPConnector(HTTP_CONNECTOR)
                              .socketBinding("http")
                              .endpoint("http-acceptor"));
        httpConnector(new HTTPConnector("http-connector-throughput")
                              .socketBinding("http")
                              .endpoint("http-acceptor-throughput")
                              .param("batch-delay", "50"));
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

    public List<RemoteConnection> remoteConnections() {
        return this.remoteConnections;
    }

    private List<RemoteConnection> remoteConnections = new ArrayList<>();

    private static final AtomicInteger COUNTER = new AtomicInteger();
}
