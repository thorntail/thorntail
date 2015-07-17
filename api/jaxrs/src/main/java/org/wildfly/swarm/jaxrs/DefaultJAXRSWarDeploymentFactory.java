package org.wildfly.swarm.jaxrs;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.wildfly.swarm.container.Container;
import org.wildfly.swarm.container.DefaultDeploymentFactory;
import org.wildfly.swarm.container.Deployment;
import org.wildfly.swarm.container.SimpleDeployment;
import org.wildfly.swarm.undertow.DefaultWarDeploymentFactory;

import java.io.File;
import java.util.UUID;

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
    public Deployment create(Container container) throws Exception {
        JAXRSArchive archive = ShrinkWrap.create( JAXRSArchive.class, determineName() );
        setup( archive );
        return new SimpleDeployment( archive );
    }
}
