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

import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.Node;
import org.jboss.shrinkwrap.api.asset.Asset;

/** An archive mix-in capable of adding JBoss Module dependencies.
 *
 * <p>This mix-in provides support for creating and modifying an internal
 * {@code jboss-deployment-structure.xml}.</p>
 *
 * @author Bob McWhirter
 * @author Ken Finnigan
 */
public interface JBossDeploymentStructureContainer<T extends Archive<T>> extends Archive<T> {
    String PRIMARY_JBOSS_DEPLOYMENT_DESCRIPTOR_PATH = "META-INF/jboss-deployment-structure.xml";
    String SECONDARY_JBOSS_DEPLOYMENT_DESCRIPTOR_PATH = "WEB-INF/jboss-deployment-structure.xml";

    /** Add a dependency on a given module, presuming the {@code main} slot.
     *
     * @param name The name of the module.
     * @return The added module descriptor.
     */
    default Module addModule(String name) {
        return getDescriptorAsset().addModule(name);
    }

    /** Add a dependency on a given module, with the specified slot.
     *
     * @param name The name of the module.
     * @param slot The slot of the module.
     * @return The added module descriptor.
     */
    default Module addModule(String name, String slot) {
        return getDescriptorAsset().addModule(name, slot);
    }

    /** Exclude a module dependency, presuming the {@code main} slot.
     *
     * @param name The name of the module.
     * @return this archive.
     */
    default T excludeModule(String name) {
        return excludeModule(name, "main");
    }

    /** Exclude a module dependency, with the specified slot.
     *
     * @param name The name of the module.
     * @param slot The slot of the module.
     * @return this archive.
     */
    @SuppressWarnings("unchecked")
    default T excludeModule(String name, String slot) {
        getDescriptorAsset().excludeModule(name, slot);

        return (T) this;
    }

    /** Retrieve the underlying {@code jboss-deployment-structure.xml} descriptor asset.
     *
     * <p>This method will effectively round-trip an existing {@code .xml} file into
     * the appropriate descriptor object tree.</p>
     *
     * @return The existing descriptor asset, if present, else a newly-created one.
     */
    default JBossDeploymentStructureAsset getDescriptorAsset() {
        String path = PRIMARY_JBOSS_DEPLOYMENT_DESCRIPTOR_PATH;
        Node jbossDS = this.get(PRIMARY_JBOSS_DEPLOYMENT_DESCRIPTOR_PATH);
        if (jbossDS == null) {
            jbossDS = this.get(SECONDARY_JBOSS_DEPLOYMENT_DESCRIPTOR_PATH);
            if (jbossDS != null) {
                path = SECONDARY_JBOSS_DEPLOYMENT_DESCRIPTOR_PATH;
            }
        }
        Asset asset;

        if (jbossDS == null) {
            asset = new JBossDeploymentStructureAsset();
        } else {
            asset = jbossDS.getAsset();
            if (!(asset instanceof JBossDeploymentStructureAsset)) {
                asset = new JBossDeploymentStructureAsset(asset.openStream());
            }
        }

        this.add(asset, path);

        return (JBossDeploymentStructureAsset) asset;
    }
}
