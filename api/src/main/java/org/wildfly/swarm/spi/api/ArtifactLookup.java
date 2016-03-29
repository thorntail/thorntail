package org.wildfly.swarm.spi.api;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;

/**
 * @author Bob McWhirter
 * @author Ken Finnigan
 */
public interface ArtifactLookup {

    AtomicReference<ArtifactLookup> INSTANCE = new AtomicReference<>();

    static ArtifactLookup get() {
        return INSTANCE.updateAndGet((e) -> {
            if (e != null) {
                return e;
            }

            try {
                return (ArtifactLookup) Class.forName("org.wildfly.swarm.internal.ArtifactManager").newInstance();
            } catch (InstantiationException e1) {
                e1.printStackTrace();
            } catch (IllegalAccessException e1) {
                e1.printStackTrace();
            } catch (ClassNotFoundException e1) {
                e1.printStackTrace();
            }
            return null;
        });
    }

    Archive artifact(String gav) throws Exception;

    Archive artifact(String gav, String asName) throws Exception;

    List<JavaArchive> allArtifacts() throws Exception;

    List<JavaArchive> allArtifacts(String... groupIdExclusions) throws Exception;
}
