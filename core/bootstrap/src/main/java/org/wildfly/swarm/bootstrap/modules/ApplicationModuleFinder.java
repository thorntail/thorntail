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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.jar.JarFile;

import org.jboss.modules.DependencySpec;
import org.jboss.modules.ModuleLoadException;
import org.jboss.modules.ModuleLoader;
import org.jboss.modules.ModuleSpec;
import org.jboss.modules.ResourceLoader;
import org.jboss.modules.ResourceLoaderSpec;
import org.jboss.modules.ResourceLoaders;
import org.jboss.modules.filter.ClassFilters;
import org.jboss.modules.filter.PathFilters;
import org.jboss.modules.maven.ArtifactCoordinates;
import org.wildfly.swarm.bootstrap.env.ApplicationEnvironment;
import org.wildfly.swarm.bootstrap.logging.BootstrapLogger;
import org.wildfly.swarm.bootstrap.util.BootstrapUtil;
import org.wildfly.swarm.bootstrap.util.JarFileManager;
import org.wildfly.swarm.bootstrap.util.TempFileManager;

/**
 * Module-finder used only for loading the module <code>swarm.application</code> when run in an fat-jar scenario.
 *
 * @author Bob McWhirter
 */
public class ApplicationModuleFinder extends AbstractSingleModuleFinder {

    public static final String MODULE_NAME = "swarm.application";

    public ApplicationModuleFinder() {
        super(MODULE_NAME);
    }

    protected ApplicationModuleFinder(String slot) {
        super(MODULE_NAME + ": " + slot);
    }

    @Override
    public void buildModule(ModuleSpec.Builder builder, ModuleLoader delegateLoader) throws ModuleLoadException {

        ApplicationEnvironment env = ApplicationEnvironment.get();

        env.bootstrapModules()
                .forEach((module) -> {
                    builder.addDependency(
                            DependencySpec.createModuleDependencySpec(
                                    PathFilters.acceptAll(),
                                    PathFilters.acceptAll(),
                                    PathFilters.acceptAll(),
                                    PathFilters.acceptAll(),
                                    ClassFilters.acceptAll(),
                                    ClassFilters.acceptAll(),
                                    null,
                                    module,
                                    false));

                });

        try {
            addAsset(builder, env);
        } catch (IOException e) {
            throw new ModuleLoadException(e);
        }

        addDependencies(builder, env);

        builder.addDependency(DependencySpec.createModuleDependencySpec("org.jboss.modules"));
        builder.addDependency(DependencySpec.createModuleDependencySpec("org.jboss.shrinkwrap"));
        builder.addDependency(DependencySpec.createModuleDependencySpec("org.wildfly.swarm.configuration", false, true));
        builder.addDependency(DependencySpec.createModuleDependencySpec("sun.jdk", false, true));

        builder.addDependency(
                DependencySpec.createModuleDependencySpec(
                        PathFilters.acceptAll(),
                        PathFilters.acceptAll(),
                        PathFilters.acceptAll(),
                        PathFilters.acceptAll(),
                        ClassFilters.acceptAll(),
                        ClassFilters.acceptAll(),
                        null,
                        "org.wildfly.swarm.container:api", true));

        builder.addDependency(DependencySpec.createLocalDependencySpec());
    }

    protected void addAsset(ModuleSpec.Builder builder, ApplicationEnvironment env) throws IOException {
        String path = env.getAsset();
        if (path == null) {
            return;
        }

        int slashLoc = path.lastIndexOf('/');
        String name = path;

        if (slashLoc > 0) {
            name = path.substring(slashLoc + 1);
        }

        String ext = ".jar";
        int dotLoc = name.lastIndexOf('.');
        if (dotLoc > 0) {
            ext = name.substring(dotLoc);
            name = name.substring(0, dotLoc);
        }

        File tmp = TempFileManager.INSTANCE.newTempFile(name, ext);

        try (InputStream artifactIn = getClass().getClassLoader().getResourceAsStream(path)) {
            Files.copy(artifactIn, tmp.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }

        final String jarName = tmp.getName().toString();
        final JarFile jarFile = new JarFile(tmp);

        File tmpDir = TempFileManager.INSTANCE.newTempDirectory(name, ext);

        //Explode jar due to some issues in Windows on stopping (JarFiles cannot be deleted)
        BootstrapUtil.explodeJar(jarFile, tmpDir.getAbsolutePath());

        jarFile.close();
        tmp.delete();

        final ResourceLoader jarLoader = ResourceLoaders.createFileResourceLoader(jarName, tmpDir);
        builder.addResourceRoot(ResourceLoaderSpec.createResourceLoaderSpec(jarLoader));

        if (".war".equalsIgnoreCase(ext)) {
            final ResourceLoader warLoader = ResourceLoaders.createFileResourceLoader(jarName + "WEBINF",
                                                                                      new File(tmpDir.getAbsolutePath() + File.separator + "WEB-INF" + File.separator + "classes"));

            builder.addResourceRoot(ResourceLoaderSpec.createResourceLoaderSpec(warLoader));
        }
    }

    protected void addDependencies(ModuleSpec.Builder builder, ApplicationEnvironment env) {
        env.getDependencies()
                .forEach((dep) -> {
                    String[] parts = dep.split(":");
                    ArtifactCoordinates coords = null;

                    if (!parts[2].equals("jar")) {
                        return;
                    }

                    if (parts.length == 4) {
                        coords = new ArtifactCoordinates(parts[0], parts[1], parts[3]);
                    } else if (parts.length == 5) {
                        coords = new ArtifactCoordinates(parts[0], parts[1], parts[4], parts[3]);
                    }
                    try {
                        File artifact = MavenResolvers.get().resolveJarArtifact(coords);
                        if (artifact == null) {
                            LOG.error("Unable to find artifact for " + coords);
                            return;
                        }
                        JarFile jar = JarFileManager.INSTANCE.addJarFile(artifact);

                        builder.addResourceRoot(
                                ResourceLoaderSpec.createResourceLoaderSpec(
                                        ResourceLoaders.createJarResourceLoader(artifact.getName(), jar)
                                )
                        );
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    private static final BootstrapLogger LOG = BootstrapLogger.logger("org.wildfly.swarm.modules.application");
}
