package org.wildfly.swarm.tools;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.jboss.shrinkwrap.api.asset.NamedAsset;
import org.wildfly.swarm.bootstrap.util.UberJarManifest;

/**
 * @author Bob McWhirter
 */
public class UberJarManifestAsset implements NamedAsset {

    private final UberJarManifest manifest;

    public UberJarManifestAsset(String mainClass) {
        this.manifest = new UberJarManifest(mainClass);
    }
    public UberJarManifestAsset(UberJarManifest manifest) {
        this.manifest = manifest;
    }
    @Override
    public String getName() {
        return "META-INF/MANIFEST.MF";
    }

    @Override
    public InputStream openStream() {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            this.manifest.write( out );
            out.close();
            ByteArrayInputStream in = new ByteArrayInputStream( out.toByteArray() );
            return in;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
