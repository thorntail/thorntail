package org.wildfly.swarm.jolokia.access;

import java.util.function.Consumer;

import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.Node;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.wildfly.swarm.undertow.WARArchive;

/**
 * @author Bob McWhirter
 */
abstract class AbstractJolokiaAccessPreparer implements Consumer<Archive> {

    public void accept(Archive archive) {
        Node node = archive.get("WEB-INF/classes/jolokia-access.xml");
        if (node == null) {
            Asset asset = getJolokiaAccessXmlAsset();
            if (asset != null) {
                archive.as(WARArchive.class).add(asset, "WEB-INF/classes/jolokia-access.xml");
            }
        }
    }

    protected abstract Asset getJolokiaAccessXmlAsset();
}
