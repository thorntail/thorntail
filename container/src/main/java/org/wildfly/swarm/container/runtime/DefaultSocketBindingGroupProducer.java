package org.wildfly.swarm.container.runtime;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Named;
import javax.inject.Singleton;

import org.wildfly.swarm.spi.api.SocketBindingGroup;

/**
 * @author Bob McWhirter
 */
@ApplicationScoped
public class DefaultSocketBindingGroupProducer {

    @Produces @Singleton @Named( "standard-sockets")
    public SocketBindingGroup standardSockets() {
        return new SocketBindingGroup( "standard-sockets", "public", "0");
    }

}
