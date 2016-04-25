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
package org.wildfly.swarm.spi.api;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.descriptor.api.Descriptors;
import org.jboss.shrinkwrap.descriptor.api.jbossdeployment13.DependenciesType;
import org.jboss.shrinkwrap.descriptor.api.jbossdeployment13.DeploymentType;
import org.jboss.shrinkwrap.descriptor.api.jbossdeployment13.ExclusionsType;
import org.jboss.shrinkwrap.descriptor.api.jbossdeployment13.JBossDeploymentStructureDescriptor;
import org.jboss.shrinkwrap.descriptor.api.jbossdeployment13.ModuleDependencyType;
import org.jboss.shrinkwrap.descriptor.api.jbossdeployment13.ModuleExclusionType;

import static org.wildfly.swarm.spi.api.ClassLoading.withTCCL;

/**
 * @author Bob McWhirter
 * @author Ken Finnigan
 */
public class JBossDeploymentStructureAsset implements Asset {

    public JBossDeploymentStructureAsset() {
        this.descriptor =
                withTCCL(Descriptors.class.getClassLoader(),
                         () -> Descriptors.create(JBossDeploymentStructureDescriptor.class));
    }

    public JBossDeploymentStructureAsset(InputStream fromStream) {
        this.descriptor =
                withTCCL(Descriptors.class.getClassLoader(),
                         () -> Descriptors.importAs(JBossDeploymentStructureDescriptor.class)
                                 .fromStream(fromStream));
    }

    public void addModule(final String name) {
        addModule(name, "main");
    }

    public void addModule(final String name, final String slot) {
        if (moduleExists(name, slot)) {
            return;
        }

        this.descriptor
                .getOrCreateDeployment()
                .getOrCreateDependencies()
                .createModule()
                .name(name)
                .slot(slot);
    }

    public void addModule(final String name, final boolean export, final String services) {
        addModule(name, "main", export, services);
    }

    public void addModule(final String name, final String slot, final boolean export, final String services) {
        if (moduleExists(name, slot, export, services)) {
            return;
        }


        ModuleDependencyType<DependenciesType<DeploymentType<JBossDeploymentStructureDescriptor>>> module =
                this.descriptor
                        .getOrCreateDeployment()
                        .getOrCreateDependencies()
                        .createModule()
                        .name(name)
                        .slot(slot)
                        .export(export);

        if (services != null && services.length() > 0) {
            module.services(services);
        }
    }

    public void excludeModule(final String name, final String slot) {
        ExclusionsType<DeploymentType<JBossDeploymentStructureDescriptor>> exclusions = this.descriptor
                .getOrCreateDeployment()
                .getOrCreateExclusions();
        List<ModuleExclusionType<ExclusionsType<DeploymentType<JBossDeploymentStructureDescriptor>>>> modules = exclusions.getAllModule();
        for (ModuleExclusionType each : modules) {
            final String existingSlot = each.getSlot();
            if (name.equals(each.getName()) &&
                    slot.equals(existingSlot == null ? "main" : existingSlot)) {

                // module already excluded
                return;
            }
        }

        exclusions.createModule()
                .name(name)
                .slot(slot);
    }

    @Override
    public InputStream openStream() {
        String output = this.descriptor.exportAsString();

        return new ByteArrayInputStream(output.getBytes());
    }

    private boolean moduleExists(final String name, final String slot) {
        return findModules(name, slot).size() > 0;
    }

    private boolean moduleExists(final String name, final String slot, final boolean export, final String services) {
        return findModules(name, slot)
                .stream()
                .filter(m -> m.isExport() == export && m.getServicesAsString().equals(services))
                .collect(Collectors.toList()).size() > 0;
    }

    private List<ModuleDependencyType<DependenciesType<DeploymentType<JBossDeploymentStructureDescriptor>>>> findModules(final String name, final String slot) {
        return this.descriptor
                .getOrCreateDeployment()
                .getOrCreateDependencies()
                .getAllModule()
                .stream()
                .filter(m -> m.getName().equals(name) && m.getSlot().equals(slot))
                .collect(Collectors.toList());
    }

    private final JBossDeploymentStructureDescriptor descriptor;
}
