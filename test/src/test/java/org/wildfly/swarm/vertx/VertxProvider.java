package org.wildfly.swarm.vertx;

import javax.annotation.Resource;
import javax.ejb.Stateless;

import io.vertx.resourceadapter.VertxConnectionFactory;

/**
 * Created by ggastald on 09/06/16.
 */
@Stateless
public class VertxProvider {

    @Resource(mappedName = "java:/eis/VertxConnectionFactory")
    VertxConnectionFactory connectionFactory;

    public VertxConnectionFactory getConnectionFactory() {
        return connectionFactory;
    }
}
