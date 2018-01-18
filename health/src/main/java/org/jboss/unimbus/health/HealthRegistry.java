package org.jboss.unimbus.health;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import org.eclipse.microprofile.health.Health;
import org.eclipse.microprofile.health.HealthCheck;
import org.jboss.unimbus.servlet.DeploymentMetaData;
import org.jboss.unimbus.servlet.HttpConstraintMetaData;
import org.jboss.unimbus.servlet.HttpMethodConstraintMetaData;
import org.jboss.unimbus.servlet.ServletMetaData;
import org.jboss.unimbus.servlet.ServletSecurityMetaData;

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
        servlet.setSecurity(
                new ServletSecurityMetaData()
                        .setHttpConstraint(new HttpConstraintMetaData()
                                                   .setEmptyRoleSemantic(ServletSecurityMetaData.EmptyRoleSemantic.DENY)
                        )
        );
        return servlet;
    }

    @Inject
    @Any
    @Health
    Instance<HealthCheck> healthChecks;
}
