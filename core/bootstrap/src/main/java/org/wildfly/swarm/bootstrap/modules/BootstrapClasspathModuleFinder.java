/**
 * Copyright 2015-2016 Red Hat, Inc, and individual contributors.
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
package org.wildfly.swarm.bootstrap.modules;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.jboss.modules.ModuleFinder;
import org.jboss.modules.ModuleIdentifier;
import org.jboss.modules.ModuleLoadException;
import org.jboss.modules.ModuleLoader;
import org.jboss.modules.ModuleSpec;
import org.jboss.modules.xml.ModuleXmlParser;
import org.wildfly.swarm.bootstrap.performance.Performance;

/**
 * Used only for loading dependencies of org.wildfly.bootstrap:main from its own jar.
 *
 * @author Bob McWhirter
 */
public class BootstrapClasspathModuleFinder implements ModuleFinder {

    private static final char MODULE_SEPARATOR = '/';

    public String toString() {
        return getClass().getSimpleName();
    }

    @Override
    public ModuleSpec findModule(ModuleIdentifier identifier, ModuleLoader delegateLoader) throws ModuleLoadException {

        try (AutoCloseable handle = Performance.accumulate("module: BootstrapClassPath")) {
            final String path = "modules/" + identifier.getName().replace('.', MODULE_SEPARATOR) + MODULE_SEPARATOR + identifier.getSlot() + "/module.xml";

            ClassLoader cl = BootstrapClasspathModuleFinder.class.getClassLoader();
            URL url = cl.getResource(path);

            if (url == null) {
                return null;
            }

            ModuleSpec moduleSpec = null;
            InputStream in = null;
            try {
                final URL base = new URL(url, "./");
                in = url.openStream();
                moduleSpec = ModuleXmlParser.parseModuleXml(
                        (rootPath, loaderPath, loaderName) -> NestedJarResourceLoader.loaderFor(base, rootPath, loaderPath, loaderName),
                        MavenResolvers.get(),
                        "/",
                        in,
                        path.toString(),
                        delegateLoader,
                        identifier);

            } catch (IOException e) {
                e.printStackTrace();
                throw new ModuleLoadException(e);
            } catch (Throwable t) {
                t.printStackTrace();
                throw t;
            } finally {
                try {
                    if (in != null) {
                        in.close();
                    }
                } catch (IOException e) {
                    throw new ModuleLoadException(e);
                }
            }
            return moduleSpec;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
}
