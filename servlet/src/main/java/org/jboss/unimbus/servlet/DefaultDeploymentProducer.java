package org.jboss.unimbus.servlet;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

/**
 * Created by bob on 1/17/18.
 */
@ApplicationScoped
public class DefaultDeploymentProducer {

    @Produces
    DeploymentMetaData defaultDeployment() {
        if ( this.servlets.isUnsatisfied() ) {
            return null;
        }
        DeploymentMetaData deployment = new DeploymentMetaData("default");
        deployment.setContextPath("/");
        deployment.addServlets( servlets );
        return deployment;
    }

    @Inject
    @Any
    Instance<ServletMetaData> servlets;
}
