package org.jboss.unimbus.jaxrs;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import io.undertow.server.handlers.PathHandler;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.ServletInfo;
import org.jboss.resteasy.cdi.CdiInjectorFactory;
import org.jboss.resteasy.cdi.ResteasyCdiExtension;
import org.jboss.resteasy.plugins.server.servlet.HttpServlet30Dispatcher;
import org.jboss.resteasy.spi.ResteasyDeployment;
import org.jboss.unimbus.events.Initialize;
import org.jboss.unimbus.undertow.UndertowDeploymentInfos;

import static io.undertow.servlet.Servlets.servlet;


/**
 * Created by bob on 1/15/18.
 */
@ApplicationScoped
public class JAXRSDeploymentInfoInitializer {

    public void deployments(@Observes @Initialize Boolean event) {
        for (Application application : this.applications) {
            DeploymentInfo deploymentInfo = createDeployment(application);
            this.deploymentInfos.add(deploymentInfo);
        }
    }

    public DeploymentInfo createDeployment(Application application) {
        ApplicationPath appPath = application.getClass().getAnnotation(ApplicationPath.class);
        String path = "/";
        if (appPath != null) path = appPath.value();
        return createDeployment(application, path);
    }

    public DeploymentInfo createDeployment(Application application, String contextPath) {
        if (contextPath == null) contextPath = "/";
        if (!contextPath.startsWith("/")) contextPath = "/" + contextPath;
        ResteasyDeployment deployment = new ResteasyDeployment();
        //deployment.setApplication(application);
        deployment.setInjectorFactoryClass(CdiInjectorFactory.class.getName());
        deployment.setActualResourceClasses(extension.getResources());
        deployment.setActualProviderClasses(extension.getProviders());
        DeploymentInfo di = createUndertowDeployment(deployment);
        di.setClassLoader(application.getClass().getClassLoader());
        di.setContextPath(contextPath);
        di.setDeploymentName("Resteasy" + contextPath);
        return di;
    }

    public DeploymentInfo createUndertowDeployment(ResteasyDeployment deployment, String mapping) {
        if (mapping == null) mapping = "/";
        if (!mapping.startsWith("/")) mapping = "/" + mapping;
        if (!mapping.endsWith("/")) mapping += "/";
        mapping = mapping + "*";
        String prefix = null;
        if (!mapping.equals("/*")) prefix = mapping.substring(0, mapping.length() - 2);
        ServletInfo resteasyServlet = servlet("ResteasyServlet", HttpServlet30Dispatcher.class)
                .setAsyncSupported(true)
                .setLoadOnStartup(1)
                .addMapping(mapping);
        if (prefix != null) resteasyServlet.addInitParam("resteasy.servlet.mapping.prefix", prefix);

        return new DeploymentInfo()
                .addServletContextAttribute(ResteasyDeployment.class.getName(), deployment)
                .addServlet(
                        resteasyServlet
                );
    }

    /**
     * Creates a web deployment for your ResteasyDeployent so you can set up things like security constraints
     * You'd call this method, add your servlet security constraints, then call createDeployment(DeploymentInfo)
     *
     * Note, only one ResteasyDeployment can be applied per DeploymentInfo.  Resteasy servlet is mapped to "/*"
     *
     * @param deployment
     * @return
     */
    public DeploymentInfo createUndertowDeployment(ResteasyDeployment deployment) {
        return createUndertowDeployment(deployment, "/");
    }


    @Inject
    Instance<Application> applications;

    @Inject
    UndertowDeploymentInfos deploymentInfos;

    @Inject
    ResteasyCdiExtension extension;

    private PathHandler root;
}
