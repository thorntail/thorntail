package org.wildfly.swarm.jaxrs.btm.runtime;

import javax.enterprise.inject.Vetoed;

import com.github.kristofa.brave.Brave;
import org.jboss.logging.Logger;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.wildfly.swarm.jaxrs.btm.BraveLookup;

/**
 * @author Heiko Braun
 * @since 19/02/16
 */
@Vetoed
public class BraveService implements BraveLookup, Service<BraveService> {

    public static final ServiceName SERVICE_NAME = ServiceName.of("swarm", "zipkin", "brave");

    private static Logger LOG = Logger.getLogger("org.wildfly.swarm.jaxrs.btm");

    public BraveService(Brave braveInstance) {
        this.brave = braveInstance;
    }

    @Override
    public Brave get() {
        return this.brave;
    }

    @Override
    public void start(StartContext startContext) throws StartException {
        LOG.info("Zipkin BTM services started: " + this.brave);
    }

    @Override
    public void stop(StopContext stopContext) {
        if (this.brave != null) {
            LOG.info("Shutdown Zipkin BTM services");
            this.brave.serverTracer().setStateNoTracing();
        }
    }

    @Override
    public BraveService getValue() throws IllegalStateException, IllegalArgumentException {
        return this;
    }


    private Brave brave;
}

