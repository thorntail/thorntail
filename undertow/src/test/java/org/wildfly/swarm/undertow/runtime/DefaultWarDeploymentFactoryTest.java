package org.wildfly.swarm.undertow.runtime;

import java.io.IOException;
import java.util.UUID;

import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.Node;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.wildfly.swarm.bootstrap.env.NativeDeploymentFactory;
import org.wildfly.swarm.container.runtime.deployments.DefaultJarDeploymentFactory;
import org.wildfly.swarm.undertow.WARArchive;
import org.wildfly.swarm.undertow.internal.RandomClass;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author Bob McWhirter
 */
public class DefaultWarDeploymentFactoryTest {


    @Test
    public void testNativeDeploymentAlreadyIsWar() throws Exception {

        NativeDeploymentFactory nativeDeploymentFactory = new NativeDeploymentFactory() {
            @Override
            public Archive nativeDeployment() throws IOException {
                WARArchive war = ShrinkWrap.create(WARArchive.class);
                war.addClass( RandomClass.class );
                return war;
            }

            @Override
            public Archive createEmptyArchive(Class<? extends Archive> type, String suffix) {
                return ShrinkWrap.create( type, UUID.randomUUID().toString() + suffix );
            }
        };

        DefaultWarDeploymentFactory factory = new DefaultWarDeploymentFactory( nativeDeploymentFactory );

        Archive war = factory.create();

        Node node = war.get("WEB-INF/classes/org/wildfly/swarm/undertow/internal/RandomClass.class");
        assertThat( node ).isNotNull();
        assertThat( node.getAsset() ).isNotNull();
    }

    @Test
    public void testNativeDeploymentPromotedFromJar() throws Exception {

        NativeDeploymentFactory nativeDeploymentFactory = new NativeDeploymentFactory() {
            @Override
            public Archive nativeDeployment() throws IOException {
                JavaArchive jar = ShrinkWrap.create(JavaArchive.class);
                jar.addClass( RandomClass.class );
                return jar;
            }

            @Override
            public Archive createEmptyArchive(Class<? extends Archive> type, String suffix) {
                return ShrinkWrap.create( type, UUID.randomUUID().toString() + suffix );
            }
        };

        DefaultWarDeploymentFactory factory = new DefaultWarDeploymentFactory( nativeDeploymentFactory );
        factory.jarDeploymentFactory = new DefaultJarDeploymentFactory( nativeDeploymentFactory );

        Archive war = factory.createFromJar();

        Node node = war.get("WEB-INF/classes/org/wildfly/swarm/undertow/internal/RandomClass.class");
        assertThat( node ).isNotNull();
        assertThat( node.getAsset() ).isNotNull();
    }
}
