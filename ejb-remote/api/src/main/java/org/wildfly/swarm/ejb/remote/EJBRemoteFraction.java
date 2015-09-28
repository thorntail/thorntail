package org.wildfly.swarm.ejb.remote;

import org.wildfly.swarm.config.ejb3.subsystem.service.Remote;
import org.wildfly.swarm.container.Container;
import org.wildfly.swarm.ejb.EJBFraction;

/**
 * @author Ken Finnigan
 */
public class EJBRemoteFraction extends EJBFraction {

    @Override
    public void initialize(Container.InitContext initContext) {
        initContext.fraction(
                createDefaultFraction()
                        .remote(
                                new Remote()
                                        .connectorRef("http-remoting-connector")
                                        .threadPoolName("default")
                        )
        );
    }
}
