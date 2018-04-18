package io.thorntail.servlet.impl;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import io.thorntail.servlet.DeploymentMetaData;
import io.thorntail.servlet.ServletMetaData;

/**
 * Created by bob on 1/17/18.
 */
@ApplicationScoped
public class DefaultDeploymentProducer {

    @Produces
    DeploymentMetaData defaultDeployment() {
        if (this.servlets.isUnsatisfied()) {
            return null;
        }
        DeploymentMetaData deployment = new DeploymentMetaData("default");
        deployment.setContextPath("/");
        deployment.addServlets(servlets);

        return deployment;
    }

    @Inject
    @Any
    Instance<ServletMetaData> servlets;

}
