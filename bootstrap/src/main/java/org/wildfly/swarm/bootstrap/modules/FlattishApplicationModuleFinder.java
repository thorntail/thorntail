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
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.jar.JarFile;

import com.sun.org.apache.bcel.internal.util.ClassLoader;
import com.sun.org.apache.xpath.internal.operations.Mod;
import org.jboss.modules.DependencySpec;
import org.jboss.modules.ModuleIdentifier;
import org.jboss.modules.ModuleLoadException;
import org.jboss.modules.ModuleLoader;
import org.jboss.modules.ModuleSpec;
import org.jboss.modules.ResourceLoaderSpec;
import org.jboss.modules.ResourceLoaders;
import org.jboss.modules.filter.ClassFilters;
import org.jboss.modules.filter.PathFilters;
import org.wildfly.swarm.bootstrap.logging.BootstrapLogger;
import org.wildfly.swarm.bootstrap.util.WildFlySwarmBootstrapConf;
import org.wildfly.swarm.bootstrap.util.WildFlySwarmClasspathConf;

/**
 * Module-finder used only for loading the first set of jars when run in an fat-jar scenario.
 *
 * @author Bob McWhirter
 */
public class FlattishApplicationModuleFinder extends ApplicationModuleFinder {

    public final static String MODULE_NAME = "swarm.application";

    public FlattishApplicationModuleFinder() {
        super("flattish");
    }

    @Override
    public void buildModule(ModuleSpec.Builder builder, ModuleLoader delegateLoader) throws ModuleLoadException {

        super.buildModule(builder, delegateLoader);

        builder.addDependency(DependencySpec.createLocalDependencySpec());
        builder.addDependency(DependencySpec.createModuleDependencySpec(ModuleIdentifier.create("org.jboss.vfs")));
        builder.addDependency(DependencySpec.createModuleDependencySpec(ModuleIdentifier.create("org.jboss.shrinkwrap")));
        builder.addDependency(DependencySpec.createModuleDependencySpec(ModuleIdentifier.create("org.wildfly.swarm.container")));
        builder.addDependency(DependencySpec.createModuleDependencySpec(ModuleIdentifier.create("org.wildfly.swarm.container", "runtime")));

        String classPath = System.getProperty("java.class.path");

        StringTokenizer tokenizer = new StringTokenizer(classPath, File.pathSeparator);

        //builder.addDependency(DependencySpec.createModuleDependencySpec(ModuleIdentifier.create("org.slf4j")));
        //builder.addDependency(DependencySpec.createModuleDependencySpec(ModuleIdentifier.create("org.slf4j.impl")));
        //builder.addDependency(DependencySpec.createModuleDependencySpec(ModuleIdentifier.create("org.slf4j.jcl-over-slf4j")));

        //builder.addDependency(DependencySpec.createModuleDependencySpec(ModuleIdentifier.create("org.jboss.logging.jul-to-slf4j-stub" )));


        try {

            WildFlySwarmClasspathConf classpathConf = new WildFlySwarmClasspathConf(ClassLoader.getSystemClassLoader());

            while (tokenizer.hasMoreTokens()) {
                String each = tokenizer.nextToken();

                Path path = Paths.get(each);
                if (Files.exists(path)) {
                    if (Files.isDirectory(path)) {
                        builder.addResourceRoot(ResourceLoaderSpec.createResourceLoaderSpec(
                                ResourceLoaders.createFileResourceLoader(path.getFileName().toString(), path.toFile())));
                    } else {
                        try {
                            JarFile jar = new JarFile(path.toFile());

                            Set<WildFlySwarmClasspathConf.Action> actions = classpathConf.getActions(jar);

                            if (actions.isEmpty()) {
                                builder.addResourceRoot(ResourceLoaderSpec.createResourceLoaderSpec(
                                        ResourceLoaders.createJarResourceLoader(path.getFileName().toString(), new JarFile(path.toFile()))));
                            } else {
                                for (WildFlySwarmClasspathConf.Action action : actions) {
                                    if (action instanceof WildFlySwarmClasspathConf.ReplaceAction) {
                                        WildFlySwarmClasspathConf.ReplaceAction replace = (WildFlySwarmClasspathConf.ReplaceAction) action;
                                        builder.addDependency(
                                                DependencySpec.createModuleDependencySpec(
                                                        ModuleIdentifier.create(replace.moduleName, replace.moduleSlot)
                                                )
                                        );
                                    }
                                }
                            }

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private static final BootstrapLogger LOG = BootstrapLogger.logger("org.wildfly.swarm.modules.application.spring");
}
