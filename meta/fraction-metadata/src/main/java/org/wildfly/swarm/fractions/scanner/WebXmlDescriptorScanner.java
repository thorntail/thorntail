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

import org.wildfly.swarm.spi.meta.FractionDetector;
import org.wildfly.swarm.spi.meta.WebXmlFractionDetector;

/**
 * @author Ken Finnigan
 */
public class WebXmlDescriptorScanner implements Scanner<InputStream> {
    @Override
    public String extension() {
        return "xml";
    }

    @Override
    public void scan(String name, final InputStream input, Collection<FractionDetector<InputStream>> detectors, Consumer<File> handleFileAsZip) throws IOException {
        if (name.endsWith("web.xml")) {
            detectors.stream()
                    .filter(d -> WebXmlFractionDetector.class.isAssignableFrom(d.getClass()))
                    .forEach(d -> {
                        if (input.markSupported()) {
                            boolean available = false;
                            boolean closed = false;
                            try {
                                available = (input.available() > 0);
                            } catch (IOException e) {
                                //input is probably already closed.
                                closed = true;
                            }

                            try {
                                if (!closed) {
                                    if (!available) {
                                        input.reset();
                                    } else {
                                        input.mark(input.available() + 1);
                                    }
                                }
                            } catch (IOException e) {
                            }
                        }

                        d.detect(input);
                    });
        }
    }
}
