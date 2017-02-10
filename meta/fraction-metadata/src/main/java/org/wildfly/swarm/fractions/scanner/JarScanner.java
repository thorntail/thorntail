package org.wildfly.swarm.fractions.scanner;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.function.Consumer;

import org.wildfly.swarm.spi.meta.FractionDetector;

/**
 * @author Ken Finnigan
 */
public class JarScanner implements Scanner<InputStream> {
    @Override
    public String extension() {
        return "jar";
    }

    @Override
    public void scan(String name, InputStream input, Collection<FractionDetector<InputStream>> detectors, Consumer<File> handleFileAsZip) throws IOException {
        final File jarFile = File.createTempFile("swarmPackageDetector", ".jar");
        jarFile.deleteOnExit();

        try (FileOutputStream out = new FileOutputStream(jarFile)) {
            copy(input, out);
        }

        handleFileAsZip.accept(jarFile);
    }
}
