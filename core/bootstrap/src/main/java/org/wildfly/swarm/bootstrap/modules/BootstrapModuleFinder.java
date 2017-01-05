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

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.jboss.modules.DependencySpec;
import org.jboss.modules.ModuleIdentifier;
import org.jboss.modules.ModuleLoadException;
import org.jboss.modules.ModuleLoader;
import org.jboss.modules.ModuleSpec;
import org.jboss.modules.ResourceLoader;
import org.jboss.modules.ResourceLoaderSpec;
import org.jboss.modules.ResourceLoaders;
import org.jboss.modules.filter.PathFilter;
import org.jboss.modules.filter.PathFilters;
import org.jboss.modules.maven.ArtifactCoordinates;
import org.wildfly.swarm.bootstrap.env.ApplicationEnvironment;
import org.wildfly.swarm.bootstrap.logging.BootstrapLogger;

/**
 * Module-finder used only for loading the first set of jars when run in an fat-jar scenario.
 *
 * @author Bob McWhirter
 */
public class BootstrapModuleFinder extends AbstractSingleModuleFinder {

    public static final String MODULE_NAME = "org.wildfly.swarm.bootstrap";

    public BootstrapModuleFinder() {
        super(MODULE_NAME);
    }

    @Override
    public void buildModule(ModuleSpec.Builder builder, ModuleLoader delegateLoader) throws ModuleLoadException {

        if (LOG.isTraceEnabled()) {
            LOG.trace("Loading module");
        }

        ApplicationEnvironment env = ApplicationEnvironment.get();

        env.bootstrapArtifacts()
                .forEach((dep) -> {
                    String[] parts = dep.split(":");
                    ArtifactCoordinates coords = null;

                    if (parts.length == 4) {
                        coords = new ArtifactCoordinates(parts[0], parts[1], parts[3]);
                    } else if (parts.length == 5) {
                        coords = new ArtifactCoordinates(parts[0], parts[1], parts[3], parts[4]);
                    }
                    try {
                        File artifact = MavenResolvers.get().resolveJarArtifact(coords);
                        if (artifact == null) {
                            throw new RuntimeException("Unable to resolve artifact from coordinates: " + coords);
                        }
                        JarFile jar = new JarFile(artifact);
                        ResourceLoader originaloader = ResourceLoaders.createJarResourceLoader(artifact.getName(), jar);

                        PathFilter filter = getModuleFilter(jar);
                        builder.addResourceRoot(
                                ResourceLoaderSpec.createResourceLoaderSpec(
                                        ResourceLoaders.createFilteredResourceLoader(filter, originaloader)
                                )
                        );
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                });

        builder.addDependency(DependencySpec.createLocalDependencySpec());
        builder.addDependency(DependencySpec.createModuleDependencySpec(ModuleIdentifier.create("org.jboss.modules")));
        builder.addDependency(DependencySpec.createModuleDependencySpec(ModuleIdentifier.create("org.jboss.msc")));
        builder.addDependency(DependencySpec.createModuleDependencySpec(ModuleIdentifier.create("org.jboss.shrinkwrap")));
        builder.addDependency(DependencySpec.createModuleDependencySpec(ModuleIdentifier.create("javax.api")));
        builder.addDependency(DependencySpec.createModuleDependencySpec(ModuleIdentifier.create("org.yaml.snakeyaml")));

        HashSet<String> paths = new HashSet<String>();
        paths.add("org/wildfly/swarm/bootstrap/logging");
        paths.add("org/wildfly/swarm/bootstrap/util");
        paths.add("org/wildfly/swarm/bootstrap/modules");
        paths.add("org/wildfly/swarm/bootstrap/env");
        builder.addDependency(DependencySpec.createSystemDependencySpec(paths, true));

    }

    private PathFilter getModuleFilter(JarFile jar) {
        Set<String> paths = new HashSet<>();

        Enumeration<JarEntry> jarEntries = jar.entries();

        while (jarEntries.hasMoreElements()) {
            JarEntry jarEntry = jarEntries.nextElement();
            if (!jarEntry.isDirectory()) {
                String name = jarEntry.getName();
                if (name.endsWith("/module.xml")) {
                    paths.add(name);
                }
            }
        }
        return PathFilters.in(paths);
    }

    private static final BootstrapLogger LOG = BootstrapLogger.logger("org.wildfly.swarm.modules.bootstrap");
}
