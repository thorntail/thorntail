package io.thorntail.datasources.impl;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import io.thorntail.datasources.DataSourceMetaData;
import io.thorntail.events.LifecycleEvent;
import io.thorntail.jca.ResourceAdapterDeployment;
import io.thorntail.jca.ResourceAdapterDeployments;

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
