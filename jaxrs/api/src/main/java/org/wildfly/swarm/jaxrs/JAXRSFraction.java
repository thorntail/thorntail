package org.wildfly.swarm.jaxrs;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.wildfly.swarm.config.JAXRS;
import org.wildfly.swarm.container.Fraction;

/**
 * @author Bob McWhirter
 */
public class JAXRSFraction extends JAXRS<JAXRSFraction> implements Fraction {

    static {
        ShrinkWrap.getDefaultDomain().getConfiguration().getExtensionLoader().addOverride( JAXRSArchive.class, JAXRSArchiveImpl.class );
    }

    public JAXRSFraction() {
    }


}
