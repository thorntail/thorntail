package org.jboss.unimbus.servlet.undertow;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import org.jboss.unimbus.servlet.DeploymentMetaData;
import org.jboss.unimbus.servlet.ServletMetaData;

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
