package org.jboss.unimbus.metrics.endpoint;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

import org.jboss.unimbus.servlet.DeploymentMetaData;
import org.jboss.unimbus.servlet.ServletMetaData;

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
