package org.jboss.unimbus.jca.ironjacamar;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.BeforeShutdown;
import javax.inject.Inject;
import javax.resource.spi.ResourceAdapter;

import org.jboss.jca.deployers.common.CommonDeployment;
import org.jboss.unimbus.events.LifecycleEvent;
import org.jboss.unimbus.jca.JCAMessages;
import org.jboss.unimbus.jca.ResourceAdapterDeployments;

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
