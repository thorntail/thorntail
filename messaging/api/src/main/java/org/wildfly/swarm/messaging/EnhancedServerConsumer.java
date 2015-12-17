package org.wildfly.swarm.messaging;

import org.wildfly.swarm.config.messaging.activemq.ServerConsumer;

/**
 * @author Bob McWhirter
 */
public interface EnhancedServerConsumer extends ServerConsumer<EnhancedServer> {
    default EnhancedServerConsumer then(EnhancedServerConsumer after) {
        return (c)->{ accept(c); after.accept(c); };
    }
}
