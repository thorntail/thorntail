package io.thorntail.metrics.impl.endpoint;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

import io.thorntail.servlet.DeploymentMetaData;
import io.thorntail.servlet.ServletMetaData;

/**
 * Created by bob on 1/22/18.
 */
@ApplicationScoped
public class EndpointProducer {

    @Produces
    @ApplicationScoped
    DeploymentMetaData deployment() {
        DeploymentMetaData deployment = new DeploymentMetaData("microprofile-metrics");
        deployment.setContextPath("/metrics");
        deployment.addServlet(servlet());
        deployment.setManagement(true);
        return deployment;
    }

    ServletMetaData servlet() {
        ServletMetaData servlet = new ServletMetaData("endpoint", MetricsServlet.class);
        servlet.addUrlPattern("/*");
        return servlet;
    }
}
