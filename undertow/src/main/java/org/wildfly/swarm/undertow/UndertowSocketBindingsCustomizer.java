package org.wildfly.swarm.undertow;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.wildfly.swarm.spi.api.Customizer;
import org.wildfly.swarm.spi.api.SocketBinding;
import org.wildfly.swarm.spi.api.SocketBindingGroup;
import org.wildfly.swarm.spi.api.SwarmProperties;

/**
 * @author Bob McWhirter
 */
@ApplicationScoped
public class UndertowSocketBindingsCustomizer implements Customizer {

    @Inject
    @Named("standard-sockets")
    private SocketBindingGroup group;

    public void customize() {
        System.err.println("** produce http socket binding on " + group.name());
        this.group.socketBinding(new SocketBinding("http")
                                         .port(SwarmProperties.propertyVar(SwarmProperties.HTTP_PORT, "8080")));
        this.group.socketBinding(new SocketBinding("https")
                                         .port(SwarmProperties.propertyVar(SwarmProperties.HTTPS_PORT, "8443")));
    }

}
