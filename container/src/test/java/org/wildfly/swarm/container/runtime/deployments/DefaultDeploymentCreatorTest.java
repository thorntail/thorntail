package org.wildfly.swarm.container.runtime.deployments;

import org.jboss.shrinkwrap.api.Archive;
import org.junit.Test;
import org.wildfly.swarm.spi.runtime.DefaultDeploymentFactory;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author Bob McWhirter
 */
public class DefaultDeploymentCreatorTest {

    @Test
    public void testNoDeploymentFactories() throws Exception {
        DefaultDeploymentCreator creator = new DefaultDeploymentCreator();
        DefaultDeploymentFactory factory = creator.getFactory("foo");
        assertThat(factory).isNotNull();
        assertThat(factory.getType()).isEqualTo("foo");

        Archive archive = factory.create();
        assertThat(archive).isNotNull();
        assertThat(archive.getName()).endsWith(".foo");
    }

    @Test
    public void testDistinctFactories() throws Exception {
        DefaultDeploymentCreator creator = new DefaultDeploymentCreator(
                new MockDefaultDeploymentFactory("war", 0),
                new MockDefaultDeploymentFactory("jar", 0)
        );

        DefaultDeploymentFactory jarFactory = creator.getFactory("jar");
        assertThat(jarFactory).isNotNull();
        assertThat(jarFactory.getType()).isEqualTo("jar");

        DefaultDeploymentFactory warFactory = creator.getFactory("war");
        assertThat(warFactory).isNotNull();
        assertThat(warFactory.getType()).isEqualTo("war");
    }

    @Test
    public void testConflictingFactories() throws Exception {
        MockDefaultDeploymentFactory lowPrio = new MockDefaultDeploymentFactory("war", 0);
        MockDefaultDeploymentFactory highPrio = new MockDefaultDeploymentFactory("war", 1000);

        DefaultDeploymentCreator creator = new DefaultDeploymentCreator( lowPrio, highPrio );
        DefaultDeploymentFactory factory = creator.getFactory( "war" );
        assertThat( factory ).isSameAs( highPrio );

        // try reverse registration

        creator = new DefaultDeploymentCreator( highPrio, lowPrio );
        factory = creator.getFactory( "war" );
        assertThat( factory ).isSameAs( highPrio );
    }
}
