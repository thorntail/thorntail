package org.wildfly.swarm.tools;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * @author Bob McWhirter
 */
public class ArtifactAsset implements ProjectAsset {

    private final ArtifactSpec spec;

    public ArtifactAsset(ArtifactSpec spec) {
        this.spec = spec;
    }

    @Override
    public String getSimpleName() {
        return this.spec.artifactId + "-" + this.spec.version + "." + this.spec.packaging;
    }

    @Override
    public InputStream openStream() {
        try {
            return new FileInputStream( spec.file );
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
}
