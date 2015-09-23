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

    default T addModule(String name, String slot) {
        Node jbossDS = this.get(PRIMARY_JBOSS_DEPLOYMENT_DESCRIPTOR_PATH);
        if (jbossDS == null) {
            jbossDS = this.get(SECONDARY_JBOSS_DEPLOYMENT_DESCRIPTOR_PATH);
            if (jbossDS != null) {
                this.delete(SECONDARY_JBOSS_DEPLOYMENT_DESCRIPTOR_PATH);
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

        this.add(asset, PRIMARY_JBOSS_DEPLOYMENT_DESCRIPTOR_PATH);

        ((JBossDeploymentStructureAsset)asset)
                .addModule(name, slot);

        return (T) this;
    }
}
