package org.wildfly.swarm.tools;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.JavaArchive;

/**
 * @author Bob McWhirter
 */
public class MockArtifactResolver implements ArtifactResolvingHelper {

    private Map<ArtifactSpec, Archive> artifacts = new HashMap<>();

    private Map<ArtifactSpec, File> resolvedArtifacts = new HashMap<>();

    public void add(String mscGav, Consumer<Archive> setup) {
        add( ArtifactSpec.fromMscGav( mscGav ), setup );
    }

    public void add(ArtifactSpec spec, Consumer<Archive> setup) {
        Archive archive = ShrinkWrap.create(JavaArchive.class);
        setup.accept( archive );
        this.artifacts.put( spec, archive );
    }

    @Override
    public ArtifactSpec resolve(ArtifactSpec spec) throws Exception {
        File resolved = resolvedArtifacts.get( spec );

        if ( resolved == null ) {
            Archive archive = artifacts.get( spec );
            if ( archive != null ) {
                resolved = File.createTempFile( spec.artifactId(), ".jar" );
                resolved.delete();
                resolved.deleteOnExit();
                archive.as(ZipExporter.class).exportTo( resolved );
                this.resolvedArtifacts.put( spec, resolved );
            }
        }
        spec.file = resolved;
        return spec;

    }

    @Override
    public Set<ArtifactSpec> resolveAll(Set<ArtifactSpec> specs) throws Exception {
        return null;
    }
}
