package org.wildfly.swarm.undertow;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

import org.wildfly.swarm.spi.api.SocketBinding;
import org.wildfly.swarm.spi.api.SwarmProperties;

/**
 * @author Bob McWhirter
 */
@ApplicationScoped
public class SocketBindingsProducer {

    public SocketBindingsProducer() {
        System.err.println( "construct socket bindings producer for undertow" );
    }

    @Produces
    public SocketBinding httpSocketBinding() {
        System.err.println( "** produce http socket binding" );
        return new SocketBinding("http")
                .port(SwarmProperties.propertyVar(SwarmProperties.HTTP_PORT, "8080"));
    }

    @Produces
    public SocketBinding httpsSocketBinding() {
        System.err.println( "** produce https socket binding" );
        return new SocketBinding("https")
                .port(SwarmProperties.propertyVar(SwarmProperties.HTTPS_PORT, "8443"));
    }

}
