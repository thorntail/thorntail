package org.jboss.unimbus.servlet.impl;

import javax.annotation.Priority;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.servlet.ServletContextListener;

import org.jboss.unimbus.events.LifecycleEvent;
import org.jboss.unimbus.servlet.DeploymentMetaData;
import org.jboss.unimbus.servlet.Deployments;

/**
 * Created by bob on 2/19/18.
 */
@ApplicationScoped
public class ServletContextListenerCustomizer {
    void customize(@Observes @Priority(1) LifecycleEvent.Initialize event) {
        this.deployments.stream().forEach(this::customize);
    }

    private void customize(DeploymentMetaData deployment) {
        for (ServletContextListener each : this.servletContextListeners) {
            deployment.addServletContextListener(each);
        }
    }


    @Inject
    Deployments deployments;

    @Inject
    @Any
    Instance<ServletContextListener> servletContextListeners;
}


