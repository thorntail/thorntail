package org.wildfly.swarm.management;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
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
public class ManagementSocketBindingsCustomizer implements Customizer {

    @Inject
    @Named("standard-sockets")
    private SocketBindingGroup group;

    public void customize() {
        this.group.socketBinding(new SocketBinding("management-http")
                .port(SwarmProperties.propertyVar(ManagementProperties.HTTP_PORT, "9990")));
        this.group.socketBinding(new SocketBinding("management-https")
                .port(SwarmProperties.propertyVar(ManagementProperties.HTTPS_PORT, "9993")));
    }
}
