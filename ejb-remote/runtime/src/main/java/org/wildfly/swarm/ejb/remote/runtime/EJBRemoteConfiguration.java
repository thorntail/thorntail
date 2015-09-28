package org.wildfly.swarm.ejb.remote.runtime;

import java.util.Collections;
import java.util.List;

import org.jboss.dmr.ModelNode;
import org.wildfly.swarm.container.runtime.AbstractServerConfiguration;
import org.wildfly.swarm.ejb.remote.EJBRemoteFraction;

/**
 * @author Ken Finnigan
 */
public class EJBRemoteConfiguration extends AbstractServerConfiguration<EJBRemoteFraction> {

    public EJBRemoteConfiguration() {
        super(EJBRemoteFraction.class);
    }

    @Override
    public boolean isIgnorable() {
        return true;
    }

    @Override
    public EJBRemoteFraction defaultFraction() {
        return new EJBRemoteFraction();
    }

    @Override
    public List<ModelNode> getList(EJBRemoteFraction fraction) {
        return Collections.emptyList();
    }
}
