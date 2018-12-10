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
package org.wildfly.swarm.spi.api;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.descriptor.api.Descriptors;
import org.jboss.shrinkwrap.descriptor.api.jbossdeployment13.DependenciesType;
import org.jboss.shrinkwrap.descriptor.api.jbossdeployment13.DeploymentType;
import org.jboss.shrinkwrap.descriptor.api.jbossdeployment13.ExclusionsType;
import org.jboss.shrinkwrap.descriptor.api.jbossdeployment13.FilterType;
import org.jboss.shrinkwrap.descriptor.api.jbossdeployment13.JBossDeploymentStructureDescriptor;
import org.jboss.shrinkwrap.descriptor.api.jbossdeployment13.ModuleDependencyType;
import org.jboss.shrinkwrap.descriptor.api.jbossdeployment13.ModuleExclusionType;
import org.jboss.shrinkwrap.descriptor.api.jbossdeployment13.PathSpecType;

import static org.wildfly.swarm.spi.api.ClassLoading.withTCCL;

/**
 * @author Bob McWhirter
 * @author Ken Finnigan
 */
public class JBossDeploymentStructureAsset implements Asset {

    public JBossDeploymentStructureAsset() {
        this.descriptor = withTCCL(Descriptors.class.getClassLoader(),
                () -> Descriptors.create(JBossDeploymentStructureDescriptor.class));
    }

    public JBossDeploymentStructureAsset(InputStream fromStream) {
        this.descriptor = withTCCL(Descriptors.class.getClassLoader(),
                () -> Descriptors.importAs(JBossDeploymentStructureDescriptor.class).fromStream(fromStream));

        // Import dependencies and exclusions into internal structure
        DeploymentType<JBossDeploymentStructureDescriptor> deployment = this.descriptor.getOrCreateDeployment();
        if (deployment != null) {
            DependenciesType<DeploymentType<JBossDeploymentStructureDescriptor>> dependencies = deployment.getOrCreateDependencies();
            if (dependencies != null) {
                this.deploymentModules.addAll(
                        dependencies.getAllModule()
                                .stream()
                                .map(this::convert)
                                .collect(Collectors.toList())
                );
            }

            ExclusionsType<DeploymentType<JBossDeploymentStructureDescriptor>> exclusions = deployment.getOrCreateExclusions();
            if (exclusions != null) {
                this.deploymentExclusions.addAll(
                        exclusions.getAllModule()
                                .stream()
                                .map(this::convert)
                                .collect(Collectors.toList())
                );
            }
        }
    }

    public Module addModule(final String name) {
        Module module = new Module(name);

        if (moduleDependencyExists(module)) {
            return module;
        }

        this.deploymentModules.add(module);
        return module;
    }

    public Module addModule(final String name, final String slot) {
        Module module = new Module(name, slot);

        if (moduleDependencyExists(module)) {
            return module;
        }

        this.deploymentModules.add(module);
        return module;
    }

    public void excludeModule(final String name) {
        this.excludeModule(name, "main");
    }

    public void excludeModule(final String name, final String slot) {
        Module module = new Module(name, slot);

        if (moduleExclusionExists(module)) {
            return;
        }

        this.deploymentExclusions.add(module);
    }

    public List<Module> deploymentModules() {
        return this.deploymentModules;
    }

    public List<Module> deploymentExclusions() {
        return this.deploymentExclusions;
    }

    @Override
    public InputStream openStream() {
        // Add modules
        DeploymentType<JBossDeploymentStructureDescriptor> deployment;

        if (this.deploymentExclusions.size() > 0 || this.deploymentModules.size() > 0) {
            deployment = this.descriptor.getOrCreateDeployment();

            deployment.getOrCreateDependencies().removeAllModule();
            for (Module deploymentModule : this.deploymentModules) {
                ModuleDependencyType<DependenciesType<DeploymentType<JBossDeploymentStructureDescriptor>>> module =
                        deployment.getOrCreateDependencies()
                                .createModule()
                                .name(deploymentModule.name())
                                .slot(deploymentModule.slot());

                if (deploymentModule.export() != null) {
                    module.export(deploymentModule.export());
                }

                if (deploymentModule.services() != null) {
                    module.services(deploymentModule.services().value());
                }

                if (deploymentModule.optional() != null) {
                    module.optional(deploymentModule.optional());
                }

                if (deploymentModule.metaInf() != null) {
                    module.metaInf(deploymentModule.metaInf());
                }

                if (deploymentModule.importIncludePaths() != null || deploymentModule.importExcludePaths() != null) {
                    FilterType<ModuleDependencyType<DependenciesType<DeploymentType<JBossDeploymentStructureDescriptor>>>> imports = module.getOrCreateImports();

                    for (String importPath : deploymentModule.importIncludePaths()) {
                        imports.createInclude()
                                .path(importPath);
                    }

                    for (String importPath : deploymentModule.importExcludePaths()) {
                        imports.createExclude()
                                .path(importPath);
                    }
                }

                if (deploymentModule.exportIncludePaths() != null || deploymentModule.exportExcludePaths() != null) {
                    FilterType<ModuleDependencyType<DependenciesType<DeploymentType<JBossDeploymentStructureDescriptor>>>> exports = module.getOrCreateExports();

                    for (String exportPath : deploymentModule.exportIncludePaths()) {
                        exports.createInclude()
                                .path(exportPath);
                    }

                    for (String exportPath : deploymentModule.exportExcludePaths()) {
                        exports.createExclude()
                                .path(exportPath);
                    }
                }
            }

            deployment.getOrCreateExclusions().removeAllModule();
            for (Module excludedModule : this.deploymentExclusions) {
                deployment.getOrCreateExclusions()
                        .createModule()
                        .name(excludedModule.name())
                        .slot(excludedModule.slot());
            }
        }

        return new ByteArrayInputStream(this.descriptor.exportAsString().getBytes());
    }

    private boolean moduleDependencyExists(Module module) {
        return this.deploymentModules.stream()
                .anyMatch(m -> m.name().equals(module.name()) && m.slot().equals(module.slot()));
    }

    private boolean moduleExclusionExists(Module module) {
        return this.deploymentExclusions.stream()
                .anyMatch(m -> m.name().equals(module.name()) && m.slot().equals(module.slot()));
    }

    private Module convert(ModuleDependencyType<DependenciesType<DeploymentType<JBossDeploymentStructureDescriptor>>> descriptorModule) {
        Module module = new Module(descriptorModule.getName(), descriptorModule.getSlot());
        module.withOptional(descriptorModule.isOptional());
        module.withExport(descriptorModule.isExport());

        if (descriptorModule.getServices() != null) {
            module.withServices(Module.ServiceHandling.valueOf(descriptorModule.getServicesAsString().toUpperCase()));
        }

        if (descriptorModule.getMetaInf() != null) {
            module.withMetaInf(descriptorModule.getMetaInfAsString());
        }

        for (FilterType<ModuleDependencyType<DependenciesType<DeploymentType<JBossDeploymentStructureDescriptor>>>> moduleImport : descriptorModule.getAllImports()) {
            moduleImport.getAllInclude()
                    .stream()
                    .map(PathSpecType::getPath)
                    .forEach(module::withImportIncludePath);
            moduleImport.getAllExclude()
                    .stream()
                    .map(PathSpecType::getPath)
                    .forEach(module::withImportExcludePath);
        }

        for (FilterType<ModuleDependencyType<DependenciesType<DeploymentType<JBossDeploymentStructureDescriptor>>>> moduleExport : descriptorModule.getAllExports()) {
            moduleExport.getAllInclude()
                    .stream()
                    .map(PathSpecType::getPath)
                    .forEach(module::withExportIncludePath);
            moduleExport.getAllExclude()
                    .stream()
                    .map(PathSpecType::getPath)
                    .forEach(module::withExportExcludePath);
        }

        return module;
    }

    private Module convert(ModuleExclusionType<ExclusionsType<DeploymentType<JBossDeploymentStructureDescriptor>>> descriptorModule) {
        return new Module(descriptorModule.getName(), descriptorModule.getSlot());
    }

    private final JBossDeploymentStructureDescriptor descriptor;

    private List<Module> deploymentModules = new ArrayList<>();

    private List<Module> deploymentExclusions = new ArrayList<>();
}
