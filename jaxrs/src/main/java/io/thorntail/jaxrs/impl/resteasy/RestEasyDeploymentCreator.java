package io.thorntail.jaxrs.impl.resteasy;

import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import io.thorntail.condition.annotation.RequiredClassPresent;
import io.thorntail.events.LifecycleEvent;
import io.thorntail.jaxrs.impl.JaxrsMessages;
import io.thorntail.servlet.DeploymentMetaData;
import io.thorntail.servlet.Deployments;
import io.thorntail.servlet.ServletMetaData;
import io.undertow.server.handlers.PathHandler;
import org.jboss.resteasy.cdi.CdiInjectorFactory;
import org.jboss.resteasy.cdi.ResteasyCdiExtension;
import org.jboss.resteasy.plugins.server.servlet.HttpServlet30Dispatcher;
import org.jboss.resteasy.spi.ResteasyDeployment;


/**
 * Created by bob on 1/15/18.
 */
@RequiredClassPresent("org.jboss.resteasy.plugins.server.servlet.HttpServlet30Dispatcher")
@ApplicationScoped
public class RestEasyDeploymentCreator {

    public void createDeployments(@Observes LifecycleEvent.Scan event) {
        for (Application application : this.applications) {
            DeploymentMetaData metaData = createDeployment(application);
            metaData.putAttachment(Application.class, application);
            this.deployments.addDeployment(metaData);
            JaxrsMessages.MESSAGES.deploymentCreated(metaData.getName());
        }
    }

    ApplicationPath findApplicationPathAnnotation(Application application) {
        Class<?> cur = application.getClass();
        while (cur != null) {
            ApplicationPath appPath = cur.getAnnotation(ApplicationPath.class);
            if (appPath != null) {
                return appPath;
            }
            cur = cur.getSuperclass();
        }

        return null;
    }

    public DeploymentMetaData createDeployment(Application application) {
        ApplicationPath appPath = findApplicationPathAnnotation(application);

        String path = "/";
        if (appPath != null) {
            path = appPath.value();
        }
        return createDeployment(application, path);
    }

    public DeploymentMetaData createDeployment(Application application, String contextPath) {
        if (contextPath == null) {
            contextPath = "/";
        }
        if (!contextPath.startsWith("/")) {
            contextPath = "/" + contextPath;
        }

        ResteasyDeployment deployment = new ResteasyDeployment();
        deployment.setInjectorFactoryClass(CdiInjectorFactory.class.getName());
        //deployment.setApplication(application);
        deployment.setScannedResourceClasses( extension.getResources().stream().map(e->e.getName()).collect(Collectors.toList()));
        deployment.setScannedProviderClasses( extension.getProviders().stream().map(e->e.getName()).collect(Collectors.toList()));
        DeploymentMetaData meta = createServletDeployment(deployment, application);
        meta.setContextPath(contextPath);
        return meta;
    }

    public DeploymentMetaData createServletDeployment(ResteasyDeployment deployment, Application application) {
        return createServletDeployment(deployment, application, "/");
    }

    public DeploymentMetaData createServletDeployment(ResteasyDeployment deployment, Application application, String mapping) {
        if (mapping == null) {
            mapping = "/";
        }
        if (!mapping.startsWith("/")) {
            mapping = "/" + mapping;
        }
        if (!mapping.endsWith("/")) {
            mapping += "/";
        }

        mapping = mapping + "*";

        String prefix = null;
        if (!mapping.equals("/*")) {
            prefix = mapping.substring(0, mapping.length() - 2);
        }

        ServletMetaData servlet = new ServletMetaData("ResteasyServlet", HttpServlet30Dispatcher.class);
        servlet.setAsyncSupported(true);
        servlet.setLoadOnStartup(1);
        servlet.addUrlPattern(mapping);
        if (prefix != null) {
            servlet.addInitParam("resteasy.servlet.mapping.prefix", prefix);
        }

        String appName = application.getClass().getSimpleName();
        int dollarLoc = appName.indexOf('$');
        if (dollarLoc > 0) {
            appName = appName.substring(0, dollarLoc);
        }
        appName = appName.replace('.', '_');

        DeploymentMetaData meta = new DeploymentMetaData("jaxrs-" + appName);

        meta.addServletContextAttribute("resteasy.role.based.security", true);

        meta.addServletContextAttribute(ResteasyDeployment.class.getName(), deployment);
        meta.addServlet(servlet);
        return meta;
    }


    @Inject
    Instance<Application> applications;

    @Inject
    Deployments deployments;

    @Inject
    ResteasyCdiExtension extension;

    private PathHandler root;
}
