package org.wildfly.swarm.jolokia.runtime;

import java.util.List;

import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.wildfly.swarm.spi.api.ArtifactLookup;

/**
 * @author Bob McWhirter
 */
public class MockArtifactLookup implements ArtifactLookup {

    @Override
    public JavaArchive artifact(String gav) throws Exception {
        return ShrinkWrap.create( JavaArchive.class, gav + ".jar" );
    }

    @Override
    public JavaArchive artifact(String gav, String asName) throws Exception {
        return ShrinkWrap.create( JavaArchive.class, asName );
    }

    @Override
    public List<JavaArchive> allArtifacts() throws Exception {
        return null;
    }

    @Override
    public List<JavaArchive> allArtifacts(String... groupIdExclusions) throws Exception {
        return null;
    }
}
