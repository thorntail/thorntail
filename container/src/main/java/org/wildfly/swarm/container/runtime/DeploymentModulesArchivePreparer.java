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
package org.wildfly.swarm.container.runtime;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.jboss.shrinkwrap.api.Archive;
import org.wildfly.swarm.spi.api.ArchivePreparer;
import org.wildfly.swarm.spi.api.Fraction;
import org.wildfly.swarm.spi.api.JARArchive;
import org.wildfly.swarm.spi.api.Module;
import org.wildfly.swarm.spi.api.annotations.DeploymentModule;
import org.wildfly.swarm.spi.api.annotations.DeploymentModules;

/**
 * @author Bob McWhirter
 */
@Singleton
public class DeploymentModulesArchivePreparer implements ArchivePreparer {

    @Inject
    @Any
    private Instance<Fraction> allFractions;

    public void prepareArchive(Archive<?> archive) {

        JARArchive jarArchive = archive.as(JARArchive.class);

        for (Fraction each : this.allFractions) {

            DeploymentModules plural = each.getClass().getAnnotation(DeploymentModules.class);

            if (plural != null) {
                DeploymentModule[] entries = plural.value();
                for (DeploymentModule entry : entries) {
                    addModule(jarArchive, entry);
                }
            } else {
                DeploymentModule entry = each.getClass().getAnnotation(DeploymentModule.class);
                if (entry != null) {
                    addModule(jarArchive, entry);
                }
            }
        }
    }

    protected void addModule(JARArchive archive, DeploymentModule entry) {
        String moduleName = entry.name();
        String moduleSlot = entry.slot();
        if (moduleSlot.equals("")) {
            moduleSlot = "main";
        }
        Module def = archive.addModule(moduleName, moduleSlot);
        def.withExport(entry.export());
        def.withMetaInf(entry.metaInf().toString().toLowerCase());
        def.withServices(entry.services());
    }
}
