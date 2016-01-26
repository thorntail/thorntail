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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Enumeration;

import org.jboss.modules.DependencySpec;
import org.jboss.modules.ModuleIdentifier;
import org.jboss.modules.ModuleLoadException;
import org.jboss.modules.ModuleLoader;
import org.jboss.modules.ModuleSpec;
import org.jboss.modules.filter.ClassFilters;
import org.jboss.modules.filter.PathFilters;
import org.wildfly.swarm.bootstrap.logging.BootstrapLogger;
import org.wildfly.swarm.bootstrap.util.Layout;
import org.wildfly.swarm.bootstrap.util.WildFlySwarmApplicationConf;

/**
 * Module-finder used only for loading the first set of jars when run in an fat-jar scenario.
 *
 * @author Bob McWhirter
 */
public class ApplicationModuleFinder extends AbstractSingleModuleFinder {

    public final static String MODULE_NAME = "swarm.application";

    private static final BootstrapLogger LOG = BootstrapLogger.logger("org.wildfly.swarm.modules.application");

    public ApplicationModuleFinder() {
        super(MODULE_NAME);
    }

    @Override
    public void buildModule(ModuleSpec.Builder builder, ModuleLoader delegateLoader) throws ModuleLoadException {
        try {
            if (Layout.getInstance().isUberJar()) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Loading as uberjar");
                }
                handleWildFlySwarmApplicationConf(builder);
            } else {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Loading as non-ubjerjar");
                }
                ClassLoader cl = ClassLoader.getSystemClassLoader();
                Enumeration<URL> results = cl.getResources("wildfly-swarm-bootstrap.conf");

                while (results.hasMoreElements()) {
                    URL each = results.nextElement();
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(each.openStream()))) {
                        String line = null;
                        while ((line = reader.readLine()) != null) {
                            line = line.trim();
                            if (!line.isEmpty()) {
                                builder.addDependency(
                                        DependencySpec.createModuleDependencySpec(
                                                PathFilters.acceptAll(),
                                                PathFilters.acceptAll(),
                                                PathFilters.acceptAll(),
                                                PathFilters.acceptAll(),
                                                ClassFilters.acceptAll(),
                                                ClassFilters.acceptAll(),
                                                null,
                                                ModuleIdentifier.create(line), false));
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            throw new ModuleLoadException(e);
        } catch (URISyntaxException e) {
            throw new ModuleLoadException(e);
        } catch (Exception e) {
            throw new ModuleLoadException(e);
        }

        builder.addDependency(DependencySpec.createModuleDependencySpec(ModuleIdentifier.create("org.jboss.modules")));
        builder.addDependency(DependencySpec.createModuleDependencySpec(ModuleIdentifier.create("org.jboss.msc")));
        builder.addDependency(DependencySpec.createModuleDependencySpec(ModuleIdentifier.create("org.jboss.shrinkwrap")));
        builder.addDependency(DependencySpec.createModuleDependencySpec(ModuleIdentifier.create("org.wildfly.swarm.configuration"), false, true));
        builder.addDependency(DependencySpec.createModuleDependencySpec(ModuleIdentifier.create("javax.api")));
        builder.addDependency(DependencySpec.createModuleDependencySpec(ModuleIdentifier.create("sun.jdk"), false, true));

        builder.addDependency(
                DependencySpec.createModuleDependencySpec(
                        PathFilters.acceptAll(),
                        PathFilters.acceptAll(),
                        PathFilters.acceptAll(),
                        PathFilters.acceptAll(),
                        ClassFilters.acceptAll(),
                        ClassFilters.acceptAll(),
                        null,
                        ModuleIdentifier.create("org.wildfly.swarm.container", "api"), true));

        builder.addDependency(DependencySpec.createLocalDependencySpec());
    }

    protected void handleWildFlySwarmApplicationConf(ModuleSpec.Builder builder) throws Exception {
        InputStream appConf = getClass().getClassLoader().getResourceAsStream(WildFlySwarmApplicationConf.CLASSPATH_LOCATION);
        if (appConf != null) {
            WildFlySwarmApplicationConf conf = new WildFlySwarmApplicationConf(appConf);
            conf.apply(builder);
        }
    }
}
