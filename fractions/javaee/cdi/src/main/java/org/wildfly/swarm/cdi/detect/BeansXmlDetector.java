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

import java.io.File;

import org.wildfly.swarm.spi.meta.FileDetector;
import org.wildfly.swarm.spi.meta.PathSource;

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
    public void detect(PathSource fileSource) {
        String relativePath = fileSource.getRelativePath();
        if (!detectionComplete() &&
                (relativePath.equals(BEANS_XML_WEB_INF_1) ||
                relativePath.equals(BEANS_XML_WEB_INF_2) || relativePath.equals(BEANS_XML_META_INF))) {
            detected = true;
            detectionComplete = true;
        }
    }

    @Override
    public String artifactId() {
        return CDI;
    }

    private static final String XML = "xml";

    private static final String BEANS_XML_WEB_INF_1 = "WEB-INF" + File.separator + "beans.xml";
    private static final String BEANS_XML_WEB_INF_2 = "WEB-INF" + File.separator + "classes" + File.separator + "META-INF" + File.separator + "beans.xml";
    private static final String BEANS_XML_META_INF = "META-INF" + File.separator + "beans.xml";

    private static final String CDI = "cdi";

    private boolean detected = false;

    private boolean detectionComplete = false;

}

