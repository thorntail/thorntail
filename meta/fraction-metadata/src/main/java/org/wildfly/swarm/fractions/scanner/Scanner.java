/**
 * Copyright 2015-2017 Red Hat, Inc, and individual contributors.
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
import java.util.Collection;
import java.util.function.Consumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.wildfly.swarm.spi.meta.PathSource;
import org.wildfly.swarm.spi.meta.ZipPathSource;
import org.wildfly.swarm.spi.meta.FractionDetector;

/**
 * @author Ken Finnigan
 */
public interface Scanner<T> {
    String extension();


    default void scan(PathSource fileSource, Collection<FractionDetector<T>> detectors, Consumer<File> handleFileAsZip) throws IOException {
    }

    default void scan(ZipEntry entry, ZipFile source, Collection<FractionDetector<T>> detectors, Consumer<File> handleFileAsZip) throws IOException {
        scan(new ZipPathSource(source, entry), detectors, handleFileAsZip);
    }

    default void copy(InputStream input, OutputStream output) throws IOException {
        int n = 0;
        byte[] buffer = new byte[1024 * 4];

        while (-1 != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
        }
    }

}
