package org.wildfly.swarm.messaging;

/**
 * @author Bob McWhirter
 */
public interface MessagingProperties {

    String DEFAULT_REMOTE_MQ_NAME = "remote-mq";
    String DEFAULT_REMOTE_HOST = "localhost";
    String DEFAULT_REMOTE_PORT = "61616";
    String DEFAULT_REMOTE_JNDI_NAME = "java:/jms/" + DEFAULT_REMOTE_MQ_NAME;

    String REMOTE = "swarm.message.remote";
    String REMOTE_MQ_NAME = "swarm.message.remote.name";
    String REMOTE_HOST = "swarm.messaging.remote.host";
    String REMOTE_PORT = "swarm.messaging.remote.port";
    String REMOTE_JNDI_NAME = "swarm.messaging.remote.jndi-name";
}
