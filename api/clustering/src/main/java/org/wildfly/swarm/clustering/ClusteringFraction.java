package org.wildfly.swarm.clustering;

import org.wildfly.swarm.container.Container;
import org.wildfly.swarm.container.Fraction;
import org.wildfly.swarm.container.SocketBinding;

/**
 * @author Bob McWhirter
 */
public class ClusteringFraction implements Fraction {

    public ClusteringFraction() {
    }

    @Override
    public void initialize(Container.InitContext initContext) {
        initContext.socketBinding(
                new SocketBinding("jgroups-udp")
                        .port(55200)
                        .multicastAddress("${jboss.default.multicast.address:230.0.0.4}")
                        .multicastPort(45688));

        initContext.socketBinding(
                new SocketBinding("jgroups-udp-fd")
                        .port(54200));

    }
}
