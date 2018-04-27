package io.thorntail.datasources.impl;

import java.io.File;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import io.thorntail.datasources.DataSourceMetaData;
import io.thorntail.jca.ResourceAdapterDeployment;

/**
 * Created by bob on 2/9/18.
 */
@ApplicationScoped
public class DeploymentBuilder {

    public ResourceAdapterDeployment build(DataSourceMetaData ds) {
        return new ResourceAdapterDeployment(
                ds.getId(),
                new File(ds.getId() + ".jar"),
                this.connectorBuilder.build(ds),
                this.activationBuilder.build(ds));

    }

    @Inject
    ConnectorBuilder connectorBuilder;

    @Inject
    ActivationBuilder activationBuilder;
}
