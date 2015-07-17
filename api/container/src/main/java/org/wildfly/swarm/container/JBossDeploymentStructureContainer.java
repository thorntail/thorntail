package org.wildfly.swarm.container;

import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.Node;

/**
 * @author Bob McWhirter
 */
public interface JBossDeploymentStructureContainer<T extends Archive<T>> extends Archive<T> {

    default T addModule(String name) {
        return addModule( name, "main" );
    }

    default T addModule(String name, String slot) {
        Node structure = this.get("META-INF/jboss-deployment-structure.xml");
        JBossDeploymentStructureAsset asset = null;
        if ( structure == null ) {
            asset = new JBossDeploymentStructureAsset();
            this.add( asset, "META-INF/jboss-deployment-structure.xml" );
        } else {
            asset = (JBossDeploymentStructureAsset) structure.getAsset();
        }

        asset.addModule( name, slot );
        return (T) this;
    }
}
