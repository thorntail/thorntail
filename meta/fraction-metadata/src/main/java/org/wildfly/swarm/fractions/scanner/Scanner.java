/**
 * Copyright 2017 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wildfly.swarm.fractions.scanner;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
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

    default void scan(Path source, Consumer<Path> childHandler) throws IOException {
        if (Files.isDirectory(source)) {
            Files.walkFileTree(source, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    childHandler.accept(file);
                    return super.visitFile(file, attrs);
                }
            });
        } else {
            childHandler.accept(source);
        }
    }

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
