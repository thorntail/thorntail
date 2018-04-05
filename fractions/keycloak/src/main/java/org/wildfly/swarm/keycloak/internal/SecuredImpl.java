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
package org.wildfly.swarm.keycloak.internal;

import org.jboss.shrinkwrap.api.Node;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.impl.base.ArchiveBase;
import org.jboss.shrinkwrap.impl.base.AssignableBase;
import org.wildfly.swarm.keycloak.Secured;
import org.wildfly.swarm.spi.api.JARArchive;
import org.wildfly.swarm.undertow.descriptors.SecurityConstraint;
import org.wildfly.swarm.undertow.descriptors.WebXmlAsset;

/**
 * @author Bob McWhirter
 * @author Ken Finnigan
 */
public class SecuredImpl extends AssignableBase<ArchiveBase<?>> implements Secured {

    /**
     * Constructs a new instance using the underlying specified archive, which is required
     *
     * @param archive
     */
    public SecuredImpl(ArchiveBase<?> archive) {
        super(archive);

        Node node = getArchive().as(JARArchive.class).get("WEB-INF/web.xml");
        if (node == null) {
            this.asset = new WebXmlAsset();
            getArchive().as(JARArchive.class).add(this.asset);
        } else {
            Asset asset = node.getAsset();
            if (!(asset instanceof WebXmlAsset)) {
                this.asset = new WebXmlAsset(asset.openStream());
                getArchive().as(JARArchive.class).add(this.asset);
            } else {
                this.asset = (WebXmlAsset) asset;
            }
        }

        // Setup web.xml
        this.asset.setContextParam("resteasy.scan", "true");
        this.asset.setLoginConfig("KEYCLOAK", "ignored");
    }

    @Override
    public SecurityConstraint protect() {
        return this.asset.protect();
    }

    @Override
    public SecurityConstraint protect(String urlPattern) {
        return this.asset.protect(urlPattern);
    }

    private WebXmlAsset asset;

}

