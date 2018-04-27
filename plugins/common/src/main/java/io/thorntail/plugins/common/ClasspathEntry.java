package io.thorntail.plugins.common;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

/**
 * Created by bob on 2/13/18.
 */
public class ClasspathEntry extends AbstractEntry {

    public ClasspathEntry(Path path, String resourcePath) {
        super(path);
        this.resourcePath = resourcePath;
    }

    @Override
    public InputStream openStream() throws IOException {
        return getClass().getClassLoader().getResourceAsStream( this.resourcePath );
    }

    private final String resourcePath;
}
