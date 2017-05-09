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

package org.wildfly.swarm.jpa.detect;

import java.io.File;

import org.wildfly.swarm.spi.meta.FileDetector;
import org.wildfly.swarm.spi.meta.FileSource;

/**
 * @author Heiko Braun
 */
public class PersistenceXmlDetector extends FileDetector {

    @Override
    public String extensionToDetect() {
        return XML;
    }

    @Override
    public boolean detectionComplete() {
        return detectionComplete;
    }

    @Override
    public boolean wasDetected() {
        return detected;
    }

    @Override
    public void detect(FileSource fileSource) {
        String relativePath = fileSource.getRelativePath();
        if (!detectionComplete() && relativePath.equals(PERSISTENCE_XML)) {
            detected = true;
            detectionComplete = true;
        }
    }

    @Override
    public String artifactId() {
        return JPA;
    }

    private static final String XML = "xml";

    private static final String PERSISTENCE_XML = "META-INF" + File.separator + "persistence.xml";

    private static final String JPA = "jpa";

    private boolean detected = false;

    private boolean detectionComplete = false;

}

