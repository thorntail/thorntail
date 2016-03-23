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

    public void addModule(final String name, final String slot) {
        DependenciesType<DeploymentType<JBossDeploymentStructureDescriptor>> dependencies = this.descriptor
                .getOrCreateDeployment()
                .getOrCreateDependencies();
        List<ModuleDependencyType<DependenciesType<DeploymentType<JBossDeploymentStructureDescriptor>>>> modules = dependencies.getAllModule();
        for (ModuleDependencyType each : modules) {
            final String existingSlot = each.getSlot();
            if (name.equals(each.getName()) &&
                    slot.equals(existingSlot == null ? "main" : existingSlot)) {

                //module exists
                return;
            }
        }

        dependencies.createModule()
                .name(name)
                .slot(slot);
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

    private final JBossDeploymentStructureDescriptor descriptor;
}
