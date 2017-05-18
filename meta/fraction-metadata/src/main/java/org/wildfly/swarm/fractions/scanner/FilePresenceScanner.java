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
import java.util.Collection;
import java.util.function.Consumer;

import org.wildfly.swarm.spi.meta.FileDetector;
import org.wildfly.swarm.spi.meta.FileSource;
import org.wildfly.swarm.spi.meta.FractionDetector;

/**
 * @author Heiko Braun
 */
public class FilePresenceScanner implements Scanner<FileSource> {
    @Override
    public String extension() {
        return XML;
    }

    /**
     * scans all xml files
     */
    public void scan(FileSource fileSource, final InputStream input, Collection<FractionDetector<FileSource>> detectors, Consumer<File> handleFileAsZip) throws IOException {
        detectors.stream()
                .filter(d -> FileDetector.class.isAssignableFrom(d.getClass()))
                .forEach(d -> d.detect(fileSource));
    }

    private static final String XML = "xml";
}

