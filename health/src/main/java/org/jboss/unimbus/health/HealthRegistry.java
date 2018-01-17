package org.jboss.unimbus.health;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import io.undertow.server.handlers.PathHandler;
import org.eclipse.microprofile.health.Health;
import org.eclipse.microprofile.health.HealthCheck;
import org.jboss.unimbus.events.LifecycleEvent;
import org.jboss.unimbus.servlet.DeploymentMetaData;
import org.jboss.unimbus.servlet.ServletMetaData;

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
        deployment.addServlet( servlet() );
        return deployment;
    }

    ServletMetaData servlet() {
        ServletMetaData servlet = new ServletMetaData("endpoint", HealthServlet.class);
        servlet.addUrlPattern("/");
        return servlet;
    }

    @Inject
    @Any
    @Health
    Instance<HealthCheck> healthChecks;
}
