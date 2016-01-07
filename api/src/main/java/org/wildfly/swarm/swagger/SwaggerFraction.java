package org.wildfly.swarm.swagger;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.wildfly.swarm.container.Fraction;

/**
 * @author Lance Ball
 */
public class SwaggerFraction implements Fraction {
    static {
        ShrinkWrap.getDefaultDomain().getConfiguration().getExtensionLoader().addOverride( SwaggerArchive.class, SwaggerArchiveImpl.class );
    }

}
