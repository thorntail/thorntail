package org.wildfly.swarm.jaxrs;

import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.wildfly.swarm.container.Container;
import org.wildfly.swarm.undertow.DefaultWarDeploymentFactory;

/**
 * @author Bob McWhirter
 */
public class DefaultJAXRSWarDeploymentFactory extends DefaultWarDeploymentFactory {

    @Override
    public int getPriority() {
        return 1000;
    }

    @Override
    public String getType() {
        return "war";
    }

    @Override
    public Archive create(Container container) throws Exception {
        JAXRSArchive archive = ShrinkWrap.create( JAXRSArchive.class, determineName() );
        setup( archive );
        return archive;
    }
}
