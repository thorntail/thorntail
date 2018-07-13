package io.thorntail.health.impl;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

import io.thorntail.servlet.DeploymentMetaData;
import io.thorntail.servlet.ServletMetaData;

/**
 * Created by bob on 1/16/18.
 */
@ApplicationScoped
public class HealthRegistry {

    @Produces
    @ApplicationScoped
    DeploymentMetaData deployment() {
        DeploymentMetaData deployment = new DeploymentMetaData("microprofile-health");
        deployment.setContextPath("/health");
        deployment.addServlet(servlet());
        deployment.setManagement(true);
        return deployment;
    }

    ServletMetaData servlet() {
        ServletMetaData servlet = new ServletMetaData("endpoint", HealthServlet.class);
        servlet.addUrlPattern("/");
        return servlet;
    }

}
