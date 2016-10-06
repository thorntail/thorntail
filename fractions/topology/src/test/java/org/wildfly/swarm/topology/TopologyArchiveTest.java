package org.wildfly.swarm.topology;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.junit.Test;
import org.wildfly.swarm.spi.api.JARArchive;
import static org.fest.assertions.Assertions.*;

/**
 * @author Bob McWhirter
 */
public class TopologyArchiveTest {

    @Test
    public void testMultipleCastingToTopologyArchive() {
        JARArchive archive = ShrinkWrap.create(JARArchive.class);
        archive.as(TopologyArchive.class).advertise("foo");

        assertThat( archive.as( TopologyArchive.class).advertisements() ).hasSize(1);
        assertThat( archive.as( TopologyArchive.class).advertisements() ).contains("foo");
    }
}
