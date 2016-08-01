package org.wildfly.swarm.undertow;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Named;

import org.wildfly.swarm.spi.api.Customizer;
import org.wildfly.swarm.spi.api.Pre;
import org.wildfly.swarm.spi.api.SocketBinding;
import org.wildfly.swarm.spi.api.SocketBindingGroup;
import org.wildfly.swarm.spi.api.SwarmProperties;

/**
 * @author Bob McWhirter
 */
@Pre
@ApplicationScoped
public class AJPCustomizer implements Customizer {

    @Inject
    @Any
    private Instance<UndertowFraction> undertowInstance;

    @Inject
    @Named("standard-sockets")
    private SocketBindingGroup group;

    public AJPCustomizer() {
    }

    public SocketBinding ajpSocketBinding() {
        return null;
    }

    public void customize() {
        UndertowFraction fraction = undertowInstance.get();
        if (fraction.isEnableAJP()) {
            fraction.subresources().servers().stream()
                    .filter(server -> server.subresources().ajpListeners().isEmpty())
                    .forEach(server -> server.ajpListener("ajp", listener -> listener.socketBinding("ajp")));

            this.group.socketBinding(new SocketBinding("ajp")
                                             .port(SwarmProperties.propertyVar(SwarmProperties.AJP_PORT, "8009")));
        }
    }

}
