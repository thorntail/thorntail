package org.wildfly.swarm.undertow;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import org.wildfly.swarm.spi.api.Customizer;
import org.wildfly.swarm.spi.api.SocketBinding;
import org.wildfly.swarm.spi.api.SwarmProperties;

/**
 * @author Bob McWhirter
 */
@ApplicationScoped
public class AJPCustomizer implements Customizer {

    @Inject
    private UndertowFraction fraction;

    public AJPCustomizer() {
    }

    @Produces @Dependent
    public SocketBinding ajpSocketBinding() {
        if (this.fraction.isEnableAJP()) {
            return new SocketBinding("ajp")
                    .port(SwarmProperties.propertyVar(SwarmProperties.AJP_PORT, "8009"));
        }

        return null;
    }

    public void customize() {
        if (this.fraction.isEnableAJP()) {
            this.fraction.subresources().servers().stream()
                    .filter(server -> server.subresources().ajpListeners().isEmpty())
                    .forEach(server -> server.ajpListener("ajp", listener -> listener.socketBinding("ajp")));
        }
    }

}
