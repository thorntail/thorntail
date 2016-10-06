package org.wildfly.swarm.jolokia.access;

import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.asset.StringAsset;

/**
 * @author Bob McWhirter
 */
public class APIJolokiaAccessPreparer extends AbstractJolokiaAccessPreparer {

    public APIJolokiaAccessPreparer(JolokiaAccess access) {
        this.access = access;
    }

    @Override
    protected Asset getJolokiaAccessXmlAsset() {
        return new StringAsset( this.access.toXML() );
    }

    private final JolokiaAccess access;

}
