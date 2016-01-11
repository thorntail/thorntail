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
package org.wildfly.swarm.undertow;

import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.Node;
import org.jboss.shrinkwrap.api.asset.Asset;

/**
 * @author Bob McWhirter
 */
public interface JBossWebContainer<T extends Archive<T>> extends Archive<T> {
    String JBOSS_WEB_PATH = "WEB-INF/jboss-web.xml";

    @SuppressWarnings("unchecked")
    default T setDefaultContextRoot() {
        JBossWebAsset asset = findJbossWebAsset();
        if (asset.isRootSet()) {

            return (T) this;
        }

        String contextRoot = System.getProperty( "wildfly.swarm.context.path" );
        if ( contextRoot == null ){
            contextRoot = "/";
        }

        setContextRoot(contextRoot);

        return (T) this;
    }

    @SuppressWarnings("unchecked")
    default T setContextRoot(String contextRoot) {
        findJbossWebAsset().setContextRoot(contextRoot);

        return (T) this;
    }

    default String getContextRoot() {
        return findJbossWebAsset().getContextRoot();
    }


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
