package org.wildfly.swarm.ejb.remote.runtime;

import org.wildfly.swarm.ejb.remote.EJBRemoteFraction;
import org.wildfly.swarm.spi.runtime.AbstractServerConfiguration;

/**
 * @author Ken Finnigan
 */
public class EJBRemoteConfiguration extends AbstractServerConfiguration<EJBRemoteFraction> {
    public EJBRemoteConfiguration() {
        super(EJBRemoteFraction.class);
    }
}
