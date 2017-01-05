package org.wildfly.swarm.jolokia.access;

import java.net.URL;

import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.asset.UrlAsset;

/**
 * @author Bob McWhirter
 */
public class URLJolokiaAccessPreparer extends AbstractJolokiaAccessPreparer {

    public URLJolokiaAccessPreparer(URL url) {
        this.url = url;
    }

    @Override
    protected Asset getJolokiaAccessXmlAsset() {
        return new UrlAsset(this.url);
    }

    private final URL url;

}
