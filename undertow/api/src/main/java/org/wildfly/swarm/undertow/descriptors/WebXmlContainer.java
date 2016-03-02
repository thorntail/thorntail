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
import org.jboss.shrinkwrap.api.asset.NamedAsset;

/**
 * @author Ken Finnigan
 */
public interface WebXmlContainer<T extends Archive<T>> extends Archive<T> {

    @SuppressWarnings("unchecked")
    default T addContextParam(String name, String... values) {
        findWebXmlAsset().setContextParam(name, values);

        return (T) this;
    }

    default WebXmlAsset findWebXmlAsset() {
        final Node webXml = this.get(WebXmlAsset.NAME);
        NamedAsset asset;
        if (webXml == null) {
            asset = new WebXmlAsset();
            this.add(asset);
        } else {
            asset = (NamedAsset) webXml.getAsset();
            if (!(asset instanceof WebXmlAsset)) {
                asset = new WebXmlAsset(asset.openStream());
                this.add(asset);
            }
        }

        return (WebXmlAsset) asset;
    }
}
