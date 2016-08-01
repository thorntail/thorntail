package org.wildfly.swarm.undertow;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.inject.Inject;
import javax.inject.Named;

import org.wildfly.swarm.spi.api.Customizer;
import org.wildfly.swarm.spi.api.Pre;
import org.wildfly.swarm.spi.api.SocketBinding;
import org.wildfly.swarm.spi.api.SocketBindingGroup;
import org.wildfly.swarm.spi.api.annotations.ConfigurationValue;

/**
 * @author Bob McWhirter
 */
@Pre
@ApplicationScoped
public class UndertowSocketBindingsCustomizer implements Customizer {

    @Inject
    @Named("standard-sockets")
    private SocketBindingGroup group;

    @Inject
    @Any
    UndertowFraction fraction;

    @Inject
    @ConfigurationValue(property = "swarm.http.port")
    Integer httpPort;

    @Inject
    @ConfigurationValue(property = "swarm.https.port")
    Integer httpsPort;

    public void customize() {
        System.err.println("** produce http socket binding on " + group.name());

        if (this.httpPort == null) {
            this.httpPort = this.fraction.httpPort();
        }

        if (this.httpsPort == null) {
            this.httpsPort = this.fraction.httpsPort();
        }

        this.group.socketBinding(new SocketBinding("http")
                .port(this.httpPort));
        this.group.socketBinding(new SocketBinding("https")
                .port(this.httpsPort));
    }

}
