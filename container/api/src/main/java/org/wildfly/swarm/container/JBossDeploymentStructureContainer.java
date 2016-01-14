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
package org.wildfly.swarm.container;

import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.Node;
import org.jboss.shrinkwrap.api.asset.Asset;

/**
 * @author Bob McWhirter
 */
public interface JBossDeploymentStructureContainer<T extends Archive<T>> extends Archive<T> {
    String PRIMARY_JBOSS_DEPLOYMENT_DESCRIPTOR_PATH = "META-INF/jboss-deployment-structure.xml";
    String SECONDARY_JBOSS_DEPLOYMENT_DESCRIPTOR_PATH = "WEB-INF/jboss-deployment-structure.xml";

    default T addModule(String name) {
        return addModule( name, "main" );
    }

    @SuppressWarnings("unchecked")
    default T addModule(String name, String slot) {
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

        ((JBossDeploymentStructureAsset)asset)
                .addModule(name, slot);

        return (T) this;
    }
}
