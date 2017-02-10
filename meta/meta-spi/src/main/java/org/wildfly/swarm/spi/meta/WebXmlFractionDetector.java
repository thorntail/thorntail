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
package org.wildfly.swarm.spi.meta;

import java.io.InputStream;
import java.util.Collection;
import java.util.HashSet;

import org.jboss.shrinkwrap.descriptor.api.Descriptors;
import org.jboss.shrinkwrap.descriptor.api.webapp31.WebAppDescriptor;
import org.jboss.shrinkwrap.descriptor.api.webcommon31.ServletType;

/**
 * @author Ken Finnigan
 */
public abstract class WebXmlFractionDetector implements FractionDetector<InputStream> {

    @Override
    public String extensionToDetect() {
        return "xml";
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
    public void detect(InputStream element) {
        if (!detectionComplete() && element != null) {
            WebAppDescriptor webXMl = Descriptors.importAs(WebAppDescriptor.class)
                    .fromStream(element);
            if (webXMl != null) {
                long servletsFound =
                        webXMl.getAllServlet()
                                .stream()
                                .map(ServletType::getServletClass)
                                .filter(c -> servletClasses.contains(c))
                                .count();
                if (servletsFound > 0) {
                    detected = true;
                    detectionComplete = true;
                }
            }
        }
    }

    public void hasServlet(String servletClass) {
        this.servletClasses.add(servletClass);
    }

    private boolean detected = false;

    private boolean detectionComplete = false;

    private Collection<String> servletClasses = new HashSet<>();
}
