package org.jboss.unimbus.jca.impl.ironjacamar;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.resource.spi.ResourceAdapter;

import org.jboss.unimbus.events.LifecycleEvent;
import org.jboss.unimbus.jca.impl.JCAMessages;
import org.jboss.unimbus.jca.ResourceAdapterDeployments;
import org.jboss.unimbus.jca.ResourceAdapterDeployment;

/**
 * Created by bob on 2/8/18.
 */
@ApplicationScoped
public class DeploymentActivator {

    void init(@Observes LifecycleEvent.Initialize event) {
        for (ResourceAdapterDeployment each : this.deployments.getDeployments()) {
            try {
                this.ras.add( this.deployer.deploy(each).getResourceAdapter() );
                JCAMessages.MESSAGES.deployedResourceAdapter(each.getUniqueId() );
            } catch (Throwable t) {
                throw new RuntimeException(t);
            }
        }
    }

    @PreDestroy
    void stop() {
        for (ResourceAdapter ra : this.ras) {
            ra.stop();
        }
    }

    @Inject
    RADeployer deployer;

    @Inject
    ResourceAdapterDeployments deployments;

    private List<ResourceAdapter> ras = new ArrayList<>();
}
