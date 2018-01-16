package org.jboss.unimbus.undertow;

import java.util.List;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.servlet.Servlet;

import io.undertow.servlet.Servlets;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.ServletInfo;
import org.jboss.unimbus.events.LifecycleEvent;

/**
 * Created by bob on 1/15/18.
 */
@ApplicationScoped
public class ServletDeploymentInfoInitializer {

    void deployments(@Observes LifecycleEvent.Scan event) {
        List<Servlet> list = this.servlets.stream().collect(Collectors.toList());
        if ( list.isEmpty() ) {
            return;
        }

        DeploymentInfo depInfo = Servlets.deployment()
                .setClassLoader(getClass().getClassLoader())
                .setContextPath("/")
                .setDeploymentName("Servlet")
                .addServlets(list.stream()
                                     .map(this::mapServletMetaData)
                                     .collect(Collectors.toSet()));
        this.deploymentInfos.add(depInfo);
    }

    private ServletInfo mapServletMetaData(Servlet servlet) {
        return Servlets.servlet(servlet.getClass().getName(), servlet.getClass()).addMapping("/" + servlet.getClass().getSimpleName().toLowerCase());
    }


    @Any
    @Inject
    private Instance<Servlet> servlets;

    @Inject
    private UndertowDeploymentInfos deploymentInfos;
}
