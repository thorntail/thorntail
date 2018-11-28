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
package org.wildfly.swarm.bootstrap.modules;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.jar.JarFile;

import org.jboss.modules.ModuleFinder;
import org.jboss.modules.ModuleLoadException;
import org.jboss.modules.ModuleLoader;
import org.jboss.modules.ModuleSpec;
import org.jboss.modules.ResourceLoaders;
import org.jboss.modules.xml.ModuleXmlParser;
import org.wildfly.swarm.bootstrap.env.ApplicationEnvironment;
import org.wildfly.swarm.bootstrap.logging.BootstrapLogger;
import org.wildfly.swarm.bootstrap.performance.Performance;

/**
 * @author Bob McWhirter
 */
public class ClasspathModuleFinder implements ModuleFinder {

    private static final char MODULE_SEPARATOR = '/';

    public String toString() {
        return getClass().getSimpleName();
    }

    @Override
    public ModuleSpec findModule(String identifier, ModuleLoader delegateLoader) throws ModuleLoadException {
        String simpleIdentifier = identifier;
        if (!identifier.contains(":")) {
            identifier = identifier + ":main";
        }
        try (AutoCloseable handle = Performance.accumulate("module: Classpath")) {
            final String[] nameAndSlot = identifier.split("\\:", 2);
            final String path = "modules/" + nameAndSlot[0].replace('.', MODULE_SEPARATOR) + MODULE_SEPARATOR + nameAndSlot[1] + "/module.xml";


            if (LOG.isTraceEnabled()) {
                LOG.trace("attempt:" + identifier);
            }

            try {
                ClassLoader cl = ApplicationEnvironment.get().getBootstrapClassLoader();
                if (LOG.isTraceEnabled()) {
                    LOG.trace("classloader: " + cl);
                    LOG.trace("path: " + path);
                }

                URL url = findResourceInClassLoader(cl, path);

                if (url == null && cl != ClasspathModuleFinder.class.getClassLoader()) {
                    url = findResourceInClassLoader(ClasspathModuleFinder.class.getClassLoader(), path);
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

                Path explodedJar = NestedJarResourceLoader.explodedJar(base);

                ModuleSpec moduleSpec = null;
                try {
                    moduleSpec = ModuleXmlParser.parseModuleXml(
                            (rootPath, loaderPath, loaderName) -> {
                                // WF14 temp files handling
                                if ("".equals(rootPath)) { // Maven artifact TODO is there a better way to recognize this?
                                    JarFile jarFile = new JarFile(loaderPath, true); // JDKSpecific.getJarFile(fp, true);
//                                    Matcher matcher = tempFilePattern.matcher(fp.getName());
//                                    if (matcher.matches()) {
//                                        JarFileManager.INSTANCE.addExisting(fp, jarFile);
//                                    }
                                    return ResourceLoaders.createJarResourceLoader(loaderName, jarFile);
                                } else { // resource root
                                    return NestedJarResourceLoader.loaderFor(base, rootPath, loaderPath, loaderName);
                                }
                            },
                            MavenResolvers.get(),
                            (explodedJar == null ? "/" : explodedJar.toAbsolutePath().toString()),
                            in,
                            path,
                            delegateLoader,
                            simpleIdentifier);

                } catch (IOException e) {
                    throw new ModuleLoadException(e);
                } finally {
                    try {
                        in.close();
                    } catch (IOException e) {
                        throw new ModuleLoadException(e);
                    }
                }
                if (LOG.isTraceEnabled()) {
                    LOG.trace("Loaded ModuleSpec: " + moduleSpec.getName());
                }
                return moduleSpec;
            } catch (IOException e) {
                throw new ModuleLoadException(e);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    private static URL findResourceInClassLoader(ClassLoader cl, String path) throws IOException {
        Enumeration<URL> resources = cl.getResources(path);
        while (resources.hasMoreElements()) {
            URL candidate = resources.nextElement();
            // always prefer productized artifacts over community ones
            if (candidate.toString().contains("redhat-")) {
                return candidate;
            }
        }

        return cl.getResource(path);
    }

    private static final BootstrapLogger LOG = BootstrapLogger.logger("org.wildfly.swarm.modules.classpath");
}
