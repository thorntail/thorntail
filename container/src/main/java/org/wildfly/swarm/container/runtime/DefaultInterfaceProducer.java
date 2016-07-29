package org.wildfly.swarm.container.runtime;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Named;

import org.wildfly.swarm.container.Interface;
import org.wildfly.swarm.spi.api.SwarmProperties;

/**
 * @author Bob McWhirter
 */
@ApplicationScoped
public class DefaultInterfaceProducer {

    @Produces
    public Interface publicInterace() {
        return new Interface( "public", SwarmProperties.propertyVar(SwarmProperties.BIND_ADDRESS, "0.0.0.0"));

    }
}
