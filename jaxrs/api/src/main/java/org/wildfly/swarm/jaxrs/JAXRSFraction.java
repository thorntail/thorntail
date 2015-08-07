package org.wildfly.swarm.jaxrs;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.impl.base.ServiceExtensionLoader;
import org.wildfly.swarm.container.Fraction;

/**
 * @author Bob McWhirter
 */
public class JAXRSFraction implements Fraction {

    static {
        ShrinkWrap.getDefaultDomain().getConfiguration().getExtensionLoader().addOverride( JAXRSArchive.class, JAXRSArchiveImpl.class );
    }

    public JAXRSFraction() {
    }


}
