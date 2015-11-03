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
                .defaultChannel("swarm-jgroups")
                .channel("swarm-jgroups", (c) -> {
                    c.stack("udp");
                })
                .stack("udp", (s) -> {
                    s.transport("UDP", (t) -> {
                        t.socketBinding("jgroups-udp");
                    });
                    s.protocol("openshift.KUBE_PING");
                }));
        container.start().stop();
    }
}
