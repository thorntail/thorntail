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
