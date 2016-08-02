package org.wildfly.swarm.jgroups;

import javax.enterprise.inject.Any;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.wildfly.swarm.container.runtime.DefaultSocketBindingGroupProducer;
import org.wildfly.swarm.spi.api.Customizer;
import org.wildfly.swarm.spi.api.Pre;
import org.wildfly.swarm.spi.api.SocketBinding;
import org.wildfly.swarm.spi.api.SocketBindingGroup;
import org.wildfly.swarm.spi.api.SwarmProperties;
import org.wildfly.swarm.spi.api.annotations.ConfigurationValue;

/**
 * @author Bob McWhirter
 */
@Pre
@Singleton
public class JGroupsSocketBindingCustomizer implements Customizer {

    @Inject
    @Named(DefaultSocketBindingGroupProducer.STANDARD_SOCKETS)
    private SocketBindingGroup group;

    @Inject
    @ConfigurationValue(JGroupsProperties.DEFAULT_MULTICAST_ADDRESS)
    private String multicastAddress;

    @Inject
    @Any
    private JGroupsFraction fraction;

    @Override
    public void customize() {
        String addr = (this.multicastAddress != null ? this.multicastAddress : this.fraction.defaultMulticastAddress());

        this.group.socketBinding(
                new SocketBinding("jgroups-udp")
                        .port(55200)
                        .multicastAddress(addr)
                        .multicastPort(45688));

        this.group.socketBinding(
                new SocketBinding("jgroups-udp-fd")
                        .port(54200));

        this.group.socketBinding(
                new SocketBinding("jgroups-mping")
                        .port(0)
                        .multicastAddress(addr)
                        .multicastPort(45700));

        this.group.socketBinding(
                new SocketBinding("jgroups-tcp")
                        .port(7600));

        this.group.socketBinding(
                new SocketBinding("jgroups-tcp-fd")
                        .port(57600));

    }
}
