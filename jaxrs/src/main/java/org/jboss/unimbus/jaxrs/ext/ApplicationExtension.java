package org.jboss.unimbus.jaxrs.ext;

import java.lang.reflect.Type;
import java.util.Set;
import java.util.function.Supplier;

import javax.annotation.Priority;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Extension;

import javax.servlet.Servlet;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import io.undertow.servlet.api.DeploymentInfo;
import org.jboss.resteasy.cdi.CdiInjectorFactory;
import org.jboss.resteasy.cdi.ResteasyCdiExtension;
import org.jboss.resteasy.plugins.server.servlet.HttpServlet30Dispatcher;
import org.jboss.resteasy.spi.ResteasyDeployment;
import org.jboss.unimbus.servlet.DeploymentMetaData;
import org.jboss.unimbus.servlet.ServletMetaData;

/**
 * Created by bob on 1/17/18.
 */
@Priority(100)
public class ApplicationExtension implements Extension {

    void process(@Observes AfterBeanDiscovery event, BeanManager beanManager) {
        Set<Bean<?>> appBeans = beanManager.getBeans(Application.class);

        System.err.println("Process Applications");
        appBeans.forEach(e -> {
            System.err.println("one: " + e);
            DeploymentMetaData meta = createDeploymentMetaData(e, event, beanManager);
            meta.setContextPath("/");
            System.err.println( "Add bean for: " + meta);
            event.addBean()
                    .scope(ApplicationScoped.class)
                    .addType(Object.class)
                    .addType(DeploymentMetaData.class)
                    .qualifiers(Any.Literal.INSTANCE)
                    .qualifiers(Default.Literal.INSTANCE)
                    .produceWith((obj) -> {
                        System.err.println("return JAXRS " + meta);
                        return meta;
                    });
        });

    }

    DeploymentMetaData createDeploymentMetaData(Bean<?> appBean, AfterBeanDiscovery event, BeanManager beanManager) {
        DeploymentMetaData meta = new DeploymentMetaData("jaxrs");
        meta.addServlet(createServletMetadata(appBean, event, beanManager));
        meta.addServletContextAttribute(ResteasyDeployment.class.getName(), createRestEasyDeployment(beanManager));
        return meta;
    }

    ServletMetaData createServletMetadata(Bean<?> appBean, AfterBeanDiscovery event, BeanManager beanManager) {
        ServletMetaData meta = new ServletMetaData(HttpServlet30Dispatcher.class, supplier());
        meta.setAsyncSupported(true);
        meta.setLoadOnStartup(1);

        processUrlPatterns(meta, appBean);

        return meta;
    }

    ResteasyDeployment createRestEasyDeployment(BeanManager beanManager) {
        ResteasyDeployment deployment = new ResteasyDeployment();
        deployment.setInjectorFactoryClass(CdiInjectorFactory.class.getName());
        deployment.setActualResourceClasses(resteasyCdiExtension(beanManager).getResources());
        deployment.setActualProviderClasses(resteasyCdiExtension(beanManager).getProviders());

        return deployment;
    }

    void processUrlPatterns(ServletMetaData meta, Bean<?> appBean) {
        String mapping = null;
        Set<Type> types = appBean.getTypes();
        for (Type type : types) {
            if (type instanceof Class) {
                ApplicationPath[] annos = (ApplicationPath[]) ((Class) type).getAnnotationsByType(ApplicationPath.class);
                for (ApplicationPath anno : annos) {
                    mapping = rectifyPath(anno.value());
                    break;
                }
            }
        }

        if (mapping == null) {
            mapping = "/*";
        }

        meta.addUrlPattern(mapping);

        String prefix = null;
        if (!mapping.equals("/*")) {
            prefix = mapping.substring(0, mapping.length() - 2);
        }
        if (prefix != null) {
            meta.addInitParam("resteasy.servlet.mapping.prefix", prefix);
        }
    }

    private String rectifyPath(String path) {
        if (!path.startsWith("/")) {
            path = "/" + path;
        }

        if (!path.endsWith("/")) {
            path = path + "/";
        }

        path = path + "*";
        return path;
    }

    private Supplier<? extends Servlet> supplier() {
        return () -> {
            return new HttpServlet30Dispatcher();
        };
    }

    ResteasyCdiExtension resteasyCdiExtension(BeanManager beanManager) {
        return beanManager.getExtension(ResteasyCdiExtension.class);
    }

    //@Inject
    //Instance<ResteasyCdiExtension> restEasyExtension;
}
