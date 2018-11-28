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

import org.jboss.modules.ModuleDependencySpecBuilder;
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
                new ModuleDependencySpecBuilder()
                        .setImportFilter(PathFilters.acceptAll())
                        .setExportFilter(PathFilters.acceptAll())
                        .setResourceImportFilter(PathFilters.acceptAll())
                        .setResourceExportFilter(PathFilters.acceptAll())
                        .setClassImportFilter(ClassFilters.acceptAll())
                        .setClassExportFilter(ClassFilters.acceptAll())
                        .setModuleLoader(null)
                        .setName("org.wildfly.swarm.spi")
                        .setOptional(false)
                        .build());

        builder.addDependency(
                new ModuleDependencySpecBuilder()
                        .setImportFilter(PathFilters.acceptAll())
                        .setExportFilter(PathFilters.acceptAll())
                        .setResourceImportFilter(PathFilters.acceptAll())
                        .setResourceExportFilter(PathFilters.acceptAll())
                        .setClassImportFilter(ClassFilters.acceptAll())
                        .setClassExportFilter(ClassFilters.acceptAll())
                        .setModuleLoader(null)
                        .setName("org.wildfly.swarm.container:runtime")
                        .setOptional(false)
                        .build());

        builder.addDependency(
                new ModuleDependencySpecBuilder()
                        .setImportFilter(PathFilters.acceptAll())
                        .setExportFilter(PathFilters.acceptAll())
                        .setResourceImportFilter(PathFilters.acceptAll())
                        .setResourceExportFilter(PathFilters.acceptAll())
                        .setClassImportFilter(ClassFilters.acceptAll())
                        .setClassExportFilter(ClassFilters.acceptAll())
                        .setModuleLoader(null)
                        .setName("org.wildfly.swarm.bootstrap")
                        .setOptional(false)
                        .build());


        builder.addDependency(
                new ModuleDependencySpecBuilder()
                        .setImportFilter(PathFilters.acceptAll())
                        .setExportFilter(PathFilters.acceptAll())
                        .setResourceImportFilter(PathFilters.acceptAll())
                        .setResourceExportFilter(PathFilters.acceptAll())
                        .setClassImportFilter(ClassFilters.acceptAll())
                        .setClassExportFilter(ClassFilters.acceptAll())
                        .setModuleLoader(null)
                        .setName("org.jboss.jandex")
                        .setOptional(false)
                        .build());

        builder.addDependency(
                new ModuleDependencySpecBuilder()
                        .setImportFilter(PathFilters.acceptAll())
                        .setExportFilter(PathFilters.acceptAll())
                        .setResourceImportFilter(PathFilters.acceptAll())
                        .setResourceExportFilter(PathFilters.acceptAll())
                        .setClassImportFilter(ClassFilters.acceptAll())
                        .setClassExportFilter(ClassFilters.acceptAll())
                        .setModuleLoader(null)
                        .setName("org.jboss.weld.se")
                        .setOptional(false)
                        .build());

        builder.addDependency(
                new ModuleDependencySpecBuilder()
                        .setImportFilter(PathFilters.acceptAll())
                        .setExportFilter(PathFilters.acceptAll())
                        .setResourceImportFilter(PathFilters.acceptAll())
                        .setResourceExportFilter(PathFilters.acceptAll())
                        .setClassImportFilter(ClassFilters.acceptAll())
                        .setClassExportFilter(ClassFilters.acceptAll())
                        .setModuleLoader(null)
                        .setName("javax.enterprise.api")
                        .setOptional(false)
                        .build());

        builder.addDependency(
                new ModuleDependencySpecBuilder()
                        .setImportFilter(PathFilters.acceptAll())
                        .setExportFilter(PathFilters.acceptAll())
                        .setResourceImportFilter(PathFilters.acceptAll())
                        .setResourceExportFilter(PathFilters.acceptAll())
                        .setClassImportFilter(ClassFilters.acceptAll())
                        .setClassExportFilter(ClassFilters.acceptAll())
                        .setModuleLoader(null)
                        .setName("org.apache.xalan")
                        .setOptional(true)
                        .build());

        builder.addDependency(
                new ModuleDependencySpecBuilder()
                        .setImportFilter(PathFilters.acceptAll())
                        .setExportFilter(PathFilters.acceptAll())
                        .setResourceImportFilter(PathFilters.acceptAll())
                        .setResourceExportFilter(PathFilters.acceptAll())
                        .setClassImportFilter(ClassFilters.acceptAll())
                        .setClassExportFilter(ClassFilters.acceptAll())
                        .setModuleLoader(null)
                        .setName("org.apache.xerces")
                        .setOptional(false)
                        .build());

        builder.addDependency(
                new ModuleDependencySpecBuilder()
                        .setImportFilter(PathFilters.acceptAll())
                        .setExportFilter(PathFilters.acceptAll())
                        .setResourceImportFilter(PathFilters.acceptAll())
                        .setResourceExportFilter(PathFilters.acceptAll())
                        .setClassImportFilter(ClassFilters.acceptAll())
                        .setClassExportFilter(ClassFilters.acceptAll())
                        .setModuleLoader(null)
                        .setName("org.codehaus.woodstox")
                        .setOptional(false)
                        .build());


        ApplicationEnvironment environment = ApplicationEnvironment.get();

        environment.bootstrapModules()
                .forEach((module) -> {
                    builder.addDependency(
                            new ModuleDependencySpecBuilder()
                                    .setImportFilter(PathFilters.acceptAll())
                                    .setExportFilter(PathFilters.acceptAll())
                                    .setResourceImportFilter(PathFilters.acceptAll())
                                    .setResourceExportFilter(PathFilters.acceptAll())
                                    .setClassImportFilter(ClassFilters.acceptAll())
                                    .setClassExportFilter(ClassFilters.acceptAll())
                                    .setModuleLoader(null)
                                    .setName(module + ":" + RUNTIME_SLOT)
                                    .setOptional(false)
                                    .build());
                });
    }

}
