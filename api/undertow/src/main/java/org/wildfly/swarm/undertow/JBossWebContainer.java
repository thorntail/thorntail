package org.wildfly.swarm.undertow;

import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.Node;

/**
 * @author Bob McWhirter
 */
public interface JBossWebContainer<T extends Archive<T>> extends Archive<T> {

    default T setDefaultContextRoot() {
        String contextRoot = System.getProperty( "wildfly.swarm.context.path" );
        if ( contextRoot == null ){
            contextRoot = "/";
        }

        setContextRoot(contextRoot);
        return (T) this;
    }

    default T setContextRoot(String contextRoot) {

        Node structure = this.get("WEB-INF/jboss-web.xml");
        JBossWebAsset asset = null;
        if ( structure == null ) {
            asset = new JBossWebAsset();
            this.add( asset, "WEB-INF/jboss-web.xml" );
        } else {
            asset = (JBossWebAsset) structure.getAsset();
        }

        asset.setContextRoot( contextRoot );
        return (T) this;
    }
}
