package org.wildfly.swarm.vertx;

/**
 * @author Bob McWhirter
 */
public interface VertxProperties {

    String DEFAULT_JNDI_NAME = "java:/eis/VertxConnectionFactory";

    String DEFAULT_CLUSTER_HOST = "localhost";

    int DEFAULT_CLUSTER_PORT = 0;
}
