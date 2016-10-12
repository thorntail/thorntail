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

import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.Node;
import org.jboss.shrinkwrap.api.asset.Asset;

/**
 * @author Bob McWhirter
 * @author Ken Finnigan
 */
public interface JBossDeploymentStructureContainer<T extends Archive<T>> extends Archive<T> {
    String PRIMARY_JBOSS_DEPLOYMENT_DESCRIPTOR_PATH = "META-INF/jboss-deployment-structure.xml";
    String SECONDARY_JBOSS_DEPLOYMENT_DESCRIPTOR_PATH = "WEB-INF/jboss-deployment-structure.xml";

    default Module addModule(String name) {
        return getDescriptorAsset().addModule(name);
    }

    default Module addModule(String name, String slot) {
        return getDescriptorAsset().addModule(name, slot);
    }

    default T excludeModule(String name) {
        return excludeModule(name, "main");
    }

    @SuppressWarnings("unchecked")
    default T excludeModule(String name, String slot) {
        getDescriptorAsset().excludeModule(name, slot);

        return (T) this;
    }

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
