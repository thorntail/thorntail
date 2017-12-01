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

import org.jboss.modules.DependencySpec;
import org.jboss.modules.ModuleLoadException;
import org.jboss.modules.ModuleLoader;
import org.jboss.modules.ModuleSpec;
import org.jboss.modules.filter.ClassFilters;
import org.jboss.modules.filter.PathFilters;
import org.wildfly.swarm.bootstrap.env.ApplicationEnvironment;

/**
 * @author Bob McWhirter
 */
public class ContainerModuleFinder extends AbstractSingleModuleFinder {

    private static final String RUNTIME_SLOT = "runtime";

    public ContainerModuleFinder() {
        super("swarm.container");
    }

    @Override
    public void buildModule(ModuleSpec.Builder builder, ModuleLoader delegateLoader) throws ModuleLoadException {
        builder.addDependency(
                DependencySpec.createModuleDependencySpec(
                        PathFilters.acceptAll(),
                        PathFilters.acceptAll(),
                        PathFilters.acceptAll(),
                        PathFilters.acceptAll(),
                        ClassFilters.acceptAll(),
                        ClassFilters.acceptAll(),
                        null,
                        "org.wildfly.swarm.spi", false));

        builder.addDependency(
                DependencySpec.createModuleDependencySpec(
                        PathFilters.acceptAll(),
                        PathFilters.acceptAll(),
                        PathFilters.acceptAll(),
                        PathFilters.acceptAll(),
                        ClassFilters.acceptAll(),
                        ClassFilters.acceptAll(),
                        null,
                        "org.wildfly.swarm.container:runtime", false));

        builder.addDependency(
                DependencySpec.createModuleDependencySpec(
                        PathFilters.acceptAll(),
                        PathFilters.acceptAll(),
                        PathFilters.acceptAll(),
                        PathFilters.acceptAll(),
                        ClassFilters.acceptAll(),
                        ClassFilters.acceptAll(),
                        null,
                        "org.wildfly.swarm.bootstrap", false));


        builder.addDependency(
                DependencySpec.createModuleDependencySpec(
                        PathFilters.acceptAll(),
                        PathFilters.acceptAll(),
                        PathFilters.acceptAll(),
                        PathFilters.acceptAll(),
                        ClassFilters.acceptAll(),
                        ClassFilters.acceptAll(),
                        null,
                        "org.jboss.jandex", false));

        builder.addDependency(
                DependencySpec.createModuleDependencySpec(
                        PathFilters.acceptAll(),
                        PathFilters.acceptAll(),
                        PathFilters.acceptAll(),
                        PathFilters.acceptAll(),
                        ClassFilters.acceptAll(),
                        ClassFilters.acceptAll(),
                        null,
                        "org.jboss.weld.se", false));

        builder.addDependency(
                DependencySpec.createModuleDependencySpec(
                        PathFilters.acceptAll(),
                        PathFilters.acceptAll(),
                        PathFilters.acceptAll(),
                        PathFilters.acceptAll(),
                        ClassFilters.acceptAll(),
                        ClassFilters.acceptAll(),
                        null,
                        "javax.enterprise.api", false));

        builder.addDependency(
                DependencySpec.createModuleDependencySpec(
                        PathFilters.acceptAll(),
                        PathFilters.acceptAll(),
                        PathFilters.acceptAll(),
                        PathFilters.acceptAll(),
                        ClassFilters.acceptAll(),
                        ClassFilters.acceptAll(),
                        null,
                        "org.apache.xalan", true));

        builder.addDependency(
                DependencySpec.createModuleDependencySpec(
                        PathFilters.acceptAll(),
                        PathFilters.acceptAll(),
                        PathFilters.acceptAll(),
                        PathFilters.acceptAll(),
                        ClassFilters.acceptAll(),
                        ClassFilters.acceptAll(),
                        null,
                        "org.apache.xerces", false));

        builder.addDependency(
                DependencySpec.createModuleDependencySpec(
                        PathFilters.acceptAll(),
                        PathFilters.acceptAll(),
                        PathFilters.acceptAll(),
                        PathFilters.acceptAll(),
                        ClassFilters.acceptAll(),
                        ClassFilters.acceptAll(),
                        null,
                        "org.codehaus.woodstox", false));


        ApplicationEnvironment environment = ApplicationEnvironment.get();

        environment.bootstrapModules()
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
                                    module + ":" + RUNTIME_SLOT, false));
                });
    }

}
