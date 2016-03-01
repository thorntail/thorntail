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
import java.net.URISyntaxException;
import java.net.URL;

import org.jboss.modules.ModuleFinder;
import org.jboss.modules.ModuleIdentifier;
import org.jboss.modules.ModuleLoadException;
import org.jboss.modules.ModuleLoader;
import org.jboss.modules.ModuleSpec;
import org.jboss.modules.xml.ModuleXmlParser;
import org.wildfly.swarm.bootstrap.logging.BootstrapLogger;
import org.wildfly.swarm.bootstrap.util.Layout;

/**
 * @author Bob McWhirter
 */
public class ClasspathModuleFinder implements ModuleFinder {

    public String toString() {
        return getClass().getSimpleName();
    }

    @Override
    public ModuleSpec findModule(ModuleIdentifier identifier, ModuleLoader delegateLoader) throws ModuleLoadException {
        final String path = "modules/" + identifier.getName().replace('.', '/') + "/" + identifier.getSlot() + "/module.xml";

        if (LOG.isTraceEnabled()) {
            LOG.trace("attempt:" + identifier);
        }

        try {
            ClassLoader cl = Layout.getInstance().getBootstrapClassLoader();
            if (LOG.isTraceEnabled()) {
                LOG.trace("classloader: " + cl);
                LOG.trace("path: " + path);
            }

            URL url = cl.getResource(path);

            if (url == null && cl != ClasspathModuleFinder.class.getClassLoader()) {
                url = ClasspathModuleFinder.class.getClassLoader().getResource(path);
            }

            if (url == null) {
                if (LOG.isTraceEnabled()) {
                    LOG.trace("not found: " + identifier);
                }
                return null;
            }

            final URL base = new URL(url, "./");

            if (LOG.isTraceEnabled()) {
                LOG.trace("base of " + identifier + ": " + base);
            }

            InputStream in = url.openStream();

            ModuleSpec moduleSpec = null;
            try {
                moduleSpec = ModuleXmlParser.parseModuleXml(
                        (rootPath, loaderPath, loaderName) -> NestedJarResourceLoader.loaderFor(base, rootPath, loaderPath, loaderName),
                        MavenResolvers.get(),
                        "/",
                        in,
                        path.toString(),
                        delegateLoader,
                        identifier);

            } catch (IOException e) {
                throw new ModuleLoadException(e);
            } finally {
                try {
                    in.close();
                } catch (IOException e) {
                    throw new ModuleLoadException(e);
                }
            }
            return moduleSpec;
        } catch (IOException e) {
            throw new ModuleLoadException(e);
        } catch (URISyntaxException e) {
            throw new ModuleLoadException(e);
        }

    }

    private static final BootstrapLogger LOG = BootstrapLogger.logger("org.wildfly.swarm.modules.classpath");
}
