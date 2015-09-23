package org.wildfly.swarm.undertow;

import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.Node;
import org.jboss.shrinkwrap.api.asset.Asset;

/**
 * @author Bob McWhirter
 */
public interface JBossWebContainer<T extends Archive<T>> extends Archive<T> {
    String JBOSS_WEB_PATH = "WEB-INF/jboss-web.xml";

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

    default T setContextRoot(String contextRoot) {
        findJbossWebAsset().setContextRoot(contextRoot);

        return (T) this;
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
