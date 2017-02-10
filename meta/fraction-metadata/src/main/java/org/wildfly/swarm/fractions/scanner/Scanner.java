package org.wildfly.swarm.fractions.scanner;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Enumeration;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.wildfly.swarm.spi.meta.FractionDetector;

/**
 * @author Ken Finnigan
 */
public interface Scanner<T> {
    String extension();

    default void scan(ZipFile source, BiConsumer<ZipEntry, ZipFile> childHandler) throws IOException {
        final Enumeration<? extends ZipEntry> entries = source.entries();
        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            if (!entry.isDirectory()) {
                childHandler.accept(entry, source);
            }
        }
    }

    default void scan(String name, InputStream input, Collection<FractionDetector<T>> detectors, Consumer<File> handleFileAsZip) throws IOException {
    }

    default void copy(InputStream input, OutputStream output) throws IOException {
        int n = 0;
        byte[] buffer = new byte[1024 * 4];

        while (-1 != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
        }
    }

}
