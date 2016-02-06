package org.wildfly.swarm.undertow.descriptors;

import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.Node;
import org.jboss.shrinkwrap.api.asset.Asset;
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
