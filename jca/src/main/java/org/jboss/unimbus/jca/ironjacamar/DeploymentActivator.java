package org.jboss.unimbus.jca.ironjacamar;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

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
                this.deployer.deploy(each);
                JCAMessages.MESSAGES.deployedResourceAdapter(each.getUniqueId() );
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        }
    }

    @Inject
    RADeployer deployer;

    @Inject
    ResourceAdapterDeployments deployments;
}
