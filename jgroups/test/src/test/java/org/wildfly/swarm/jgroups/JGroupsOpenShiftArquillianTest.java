package org.wildfly.swarm.jgroups;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.swarm.ContainerFactory;
import org.wildfly.swarm.container.Container;
import org.wildfly.swarm.container.JARArchive;

@RunWith(Arquillian.class)
public class JGroupsOpenShiftArquillianTest implements ContainerFactory {

    @Deployment(testable = false)
    public static Archive createDeployment() {
        JARArchive deployment = ShrinkWrap.create(JARArchive.class);
        deployment.add(EmptyAsset.INSTANCE, "nothing");
        return deployment;
    }

    @Override
    public Container newContainer(String... args) throws Exception {
        return new Container().fraction(new JGroupsFraction()
                .defaultChannel(new Channel("swarm-jgroups"))
                .defaultStack(new Stack("udp")
                        .transport(Transports.UDP("jgroups-udp"))
                        .protocol(Protocols.KUBE_PING())));
    }

    @Test
    @RunAsClient
    public void testNothing() {

    }
}
