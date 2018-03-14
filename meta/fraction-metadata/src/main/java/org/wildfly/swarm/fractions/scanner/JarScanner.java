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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.function.Consumer;

import org.wildfly.swarm.spi.meta.FractionDetector;
import org.wildfly.swarm.spi.meta.PathSource;

/**
 * @author Ken Finnigan
 */
public class JarScanner implements Scanner<InputStream> {
    @Override
    public String extension() {
        return "jar";
    }

    @Override
    public void scan(PathSource pathSource, Collection<FractionDetector<InputStream>> detectors, Consumer<File> handleFileAsZip) throws IOException {

        final File jarFile = File.createTempFile("swarmPackageDetector", ".jar");

        try {
            try (InputStream input = pathSource.getInputStream();
                    FileOutputStream out = new FileOutputStream(jarFile)) {
                copy(input, out);
            }

            handleFileAsZip.accept(jarFile);
        } finally {
            jarFile.delete();
        }
      }
}
