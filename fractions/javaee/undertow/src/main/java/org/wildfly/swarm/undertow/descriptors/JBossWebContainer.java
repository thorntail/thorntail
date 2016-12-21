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
package org.wildfly.swarm.undertow.descriptors;

import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.Node;
import org.jboss.shrinkwrap.api.asset.Asset;

/**
 * An archive mix-in supporting {@code jboss-web.xml} capabilities.
 *
 * @author Bob McWhirter
 */
public interface JBossWebContainer<T extends Archive<T>> extends Archive<T> {
    String JBOSS_WEB_PATH = "WEB-INF/jboss-web.xml";

    /**
     * Set the context root of this deployments.
     *
     * @param contextRoot The context root.
     * @return This archive.
     */
    @SuppressWarnings("unchecked")
    default T setContextRoot(String contextRoot) {
        findJbossWebAsset().setContextRoot(contextRoot);

        return (T) this;
    }

    /**
     * Retrieve the context root of this deployment.
     *
     * @return The context root.
     */
    default String getContextRoot() {
        return findJbossWebAsset().getContextRoot();
    }

    /**
     * Retrieve the security domain of this deployment.
     *
     * @return The security domain.
     */
    default String getSecurityDomain() {
        return findJbossWebAsset().getSecurityDomain();
    }

    /**
     * Set the security domain of this deployment.
     *
     * @param securityDomain The security domain.
     * @return This archive.
     */
    @SuppressWarnings("unchecked")
    default T setSecurityDomain(String securityDomain) {
        findJbossWebAsset().setSecurityDomain(securityDomain);

        return (T) this;
    }

    /**
     * Locate and load, or create a {@code jboss-web.xml} asset for this archive.
     *
     * @return The existing or new {@code jboss-web.xml} asset.
     */
    default JBossWebAsset findJbossWebAsset() {
        final Node jbossWeb = this.get(JBOSS_WEB_PATH);
        Asset asset;
        if (jbossWeb == null) {
            asset = new JBossWebAsset();
            this.add(asset, JBOSS_WEB_PATH);
        } else {
            asset = jbossWeb.getAsset();
            if (!(asset instanceof JBossWebAsset)) {
                asset = new JBossWebAsset(asset.openStream());
                this.add(asset, JBOSS_WEB_PATH);
            }
        }

        return (JBossWebAsset) asset;
    }
}
