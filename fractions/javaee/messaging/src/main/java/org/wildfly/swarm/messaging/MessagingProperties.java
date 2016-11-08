package org.wildfly.swarm.messaging;

/**
 * @author Bob McWhirter
 */
public interface MessagingProperties {

    String DEFAULT_REMOTE_MQ_NAME = "remote-mq";
    String DEFAULT_REMOTE_JNDI_NAME = "java:/jms/" + DEFAULT_REMOTE_MQ_NAME;
    String DEFAULT_REMOTE_HOST = "localhost";
    int DEFAULT_REMOTE_PORT = 61616;


}
