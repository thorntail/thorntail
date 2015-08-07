package org.wildfly.swarm.naming;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.swarm.container.Container;
import org.wildfly.swarm.container.JARArchive;

/**
 * @author Bob McWhirter
 */
@RunWith(Arquillian.class)
public class ArquillianTest {

    @Deployment(testable = false)
    public static Archive createDeployment() {
        JARArchive archive = ShrinkWrap.create(JARArchive.class, "test.jar");
        archive.add(EmptyAsset.INSTANCE, "nothing");
        return archive;
    }

    @Test
    @RunAsClient
    public void testContainer() throws Exception {

    }
}
