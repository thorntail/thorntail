package org.wildfly.swarm.jgroups;

import org.junit.Test;
import org.wildfly.swarm.container.Container;

public class JGroupsInVmTest {

    @Test
    public void testDefaultFraction() throws Exception {
        Container container = new Container();
        // By not specifying a ClusteringFraction we test the default fraction
        container.start().stop();
    }

    @Test
    public void testCanFindKubePing() throws Exception {
        Container container = new Container();
        container.fraction(new JGroupsFraction()
                .defaultChannel(new Channel("swarm-jgroups"))
                .defaultStack(new Stack("udp")
                        .transport(Transports.UDP("jgroups-udp"))
                        .protocol(Protocols.KUBE_PING())));
        container.start().stop();
    }
}
