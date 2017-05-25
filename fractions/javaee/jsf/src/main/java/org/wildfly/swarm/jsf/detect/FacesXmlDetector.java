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

package org.wildfly.swarm.jsf.detect;

import java.io.File;

import org.wildfly.swarm.spi.meta.FileDetector;
import org.wildfly.swarm.spi.meta.PathSource;

/**
 * @author Heiko Braun
 */
public class FacesXmlDetector extends FileDetector {

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
    public void detect(PathSource fileSource) {
        String relativePath = fileSource.getRelativePath();
        if (!detectionComplete() &&
                (relativePath.equals(FACES_CONFIG_XML_WEB_INF) || relativePath.equals(FACES_CONFIG_XML_META_INF) ||
                relativePath.endsWith(FACES_CONFIG_SUFFIX))) {
            detected = true;
            detectionComplete = true;
        }
    }

    @Override
    public String artifactId() {
        return JSF;
    }

    private static final String XML = "xml";

    private static final String FACES_CONFIG_XML_WEB_INF = "WEB-INF" + File.separator + "faces-config.xml";
    private static final String FACES_CONFIG_XML_META_INF = "META-INF" + File.separator + "faces-config.xml";
    private static final String FACES_CONFIG_SUFFIX = ".faces-config.xml";

    private static final String JSF = "jsf";

    private boolean detected = false;

    private boolean detectionComplete = false;

}

