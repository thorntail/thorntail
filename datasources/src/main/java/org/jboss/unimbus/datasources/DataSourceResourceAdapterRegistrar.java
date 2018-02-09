package org.jboss.unimbus.datasources;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.jboss.unimbus.datasources.deployment.DeploymentBuilder;
import org.jboss.unimbus.events.LifecycleEvent;
import org.jboss.unimbus.jca.ResourceAdapterDeployments;
import org.jboss.unimbus.jca.ironjacamar.ResourceAdapterDeployment;

/**
 * Created by bob on 2/9/18.
 */
@ApplicationScoped
public class DataSourceResourceAdapterRegistrar {

    void init(@Observes LifecycleEvent.Scan event) {
        for (DataSourceMetaData each : this.registry) {
            init(each);
        }
    }

    private void init(DataSourceMetaData ds) {
        ResourceAdapterDeployment deployment = this.builder.build(ds);
        this.deployments.addDeployment(deployment);
    }

    @Inject
    DeploymentBuilder builder;

    @Inject
    ResourceAdapterDeployments deployments;

    @Inject
    DataSourceRegistry registry;

}
