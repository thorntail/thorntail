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

package org.wildfly.swarm.cdi.detect;

import org.wildfly.swarm.spi.meta.FileDetector;

/**
 * @author Heiko Braun
 */
public class BeansXmlDetector extends FileDetector {

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
    public void detect(String fileName) {
        if (!detectionComplete() && fileName.endsWith(BEANS_XML)) {
            detected = true;
            detectionComplete = true;
        }
    }

    @Override
    public String artifactId() {
        return CDI;
    }

    private static final String XML = "xml";

    private static final String BEANS_XML = "beans.xml";

    private static final String CDI = "cdi";

    private boolean detected = false;

    private boolean detectionComplete = false;

}

