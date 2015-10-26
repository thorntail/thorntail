/**
 * Copyright 2015 Red Hat, Inc, and individual contributors.
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

/**
 * @author Bob McWhirter
 */
public interface StaticContentContainer<T extends Archive<T>> extends Archive<T> {

    default T staticContent() {
        return staticContent( "/", "." );
    }

    default T staticContent(String context) {
        return staticContent( context, "." );
    }

    default T staticContent(String context, String base) {
        as(WARArchive.class).addModule("org.wildfly.swarm.undertow", "runtime");
        as(WARArchive.class).addAsServiceProvider("io.undertow.server.handlers.builder.HandlerBuilder", "org.wildfly.swarm.undertow.runtime.StaticHandlerBuilder");

        Node node = as(WARArchive.class).get("WEB-INF/undertow-handlers.conf");

        UndertowHandlersAsset asset = null;
        if ( node == null ) {
            asset = new UndertowHandlersAsset();
            as(WARArchive.class).add( asset, "WEB-INF/undertow-handlers.conf" );
        } else {
            asset = (UndertowHandlersAsset) node.getAsset();
        }

        asset.staticContent( context, base );

        return (T) this;
    }
}
