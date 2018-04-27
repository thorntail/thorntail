package io.thorntail.plugins.common;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.jar.Manifest;

/**
 * Created by bob on 2/13/18.
 */
public class ManifestEntry extends AbstractEntry {

    ManifestEntry(Manifest manifest) {
        super(Paths.get("META-INF/MANIFEST.MF"));
        this.manifest = manifest;
    }

    @Override
    public InputStream openStream() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        this.manifest.write(out);
        return new ByteArrayInputStream( out.toByteArray() );
    }

    private final Manifest manifest;
}
