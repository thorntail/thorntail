package org.wildfly.swarm.clustering;

import org.junit.Test;
import org.wildfly.swarm.container.Container;

public class ClusteringInVmTest {

    @Test
    public void testDefaultFraction() throws Exception {
        Container container = new Container();
        // By not specifying a ClusteringFraction we test the default fraction
        container.start().stop();
    }

    @Test
    public void testCanFindKubePing() throws Exception {
        Container container = new Container();
        container.fraction(new ClusteringFraction()
                .defaultChannel(new Channel("swarm-clustering"))
                .defaultStack(new Stack("udp")
                        .transport(Transports.UDP("jgroups-udp"))
                        .protocol(Protocols.KUBE_PING())));
        container.start().stop();
    }
}
