package org.wildfly.swarm.mod_cluster.runtime;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.wildfly.swarm.container.runtime.config.DefaultSocketBindingGroupProducer;
import org.wildfly.swarm.mod_cluster.ModclusterFraction;
import org.wildfly.swarm.spi.api.Customizer;
import org.wildfly.swarm.spi.api.SocketBinding;
import org.wildfly.swarm.spi.api.SocketBindingGroup;
import org.wildfly.swarm.spi.runtime.annotations.Pre;

/**
 * @author Bob McWhirter
 */
@Pre
@ApplicationScoped
public class ModclusterSocketBindingCustomizer implements Customizer {

    @Inject
    @Named(DefaultSocketBindingGroupProducer.STANDARD_SOCKETS)
    private SocketBindingGroup group;

    @Inject
    private ModclusterFraction fraction;

    @Override
    public void customize() {
        this.group.socketBinding(
                new SocketBinding("modcluster")
                        .port(0)
                        .multicastAddress(this.fraction.multicastAddress())
                        .multicastPort(this.fraction.multicastPort()));
    }
}
