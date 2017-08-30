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
package org.wildfly.swarm.container.runtime;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;

/**
 * @author Bob McWhirter
 */
public class ExtensionPreventionClassLoaderWrapper extends ClassLoader {

    private static final String EXTENSION = "META-INF/services/javax.enterprise.inject.spi.Extension";

    public ExtensionPreventionClassLoaderWrapper(ClassLoader delegate) {
        super(delegate);
    }

    @Override
    public Enumeration<URL> getResources(String name) throws IOException {

        if (name.equals(EXTENSION)) {
            return Collections.emptyEnumeration();
        }

        return super.getResources(name);
    }

}

