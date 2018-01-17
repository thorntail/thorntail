package org.jboss.unimbus.servlet.ext;

import java.lang.reflect.Type;
import java.util.Set;
import java.util.function.Supplier;

import javax.enterprise.context.Dependent;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Extension;
import javax.servlet.Servlet;
import javax.servlet.annotation.WebServlet;

import org.jboss.unimbus.servlet.ServletMetaData;

/**
 * Created by bob on 1/17/18.
 */
public class ServletExtension implements Extension {

    void createServletMetaData(@Observes AfterBeanDiscovery event, BeanManager beanManager) {
        beanManager.getBeans(Servlet.class).forEach(e -> {
            createServletMetaData((Bean<Servlet>) e, event, beanManager);
        });
    }

    void createServletMetaData(Bean<Servlet> servletBean, AfterBeanDiscovery event, BeanManager beanManager) {
        System.err.println("process: " + servletBean);
        ServletMetaData meta = new ServletMetaData(getType(servletBean), supplier(servletBean, beanManager));

        processUrlPatterns(meta, servletBean);

        System.err.println("meta; " + meta);

        event.addBean()
                .scope(Dependent.class)
                .addQualifier(Default.Literal.INSTANCE)
                .addType(Object.class)
                .addType(ServletMetaData.class)
                .addType(ServletMetaData.class)
                .produceWith((obj) -> {
                    System.err.println("PRODUCE WITH: " + obj + " returning " + meta);
                    return meta;
                });
    }

    Class<? extends Servlet> getType(Bean<Servlet> servletBean) {
        int curDepth = 0;
        Class<? extends Servlet> curClass = null;
        for (Type type : servletBean.getTypes()) {
            if (type instanceof Class) {
                if (!((Class) type).isInterface()) {
                    if (Servlet.class.isAssignableFrom((Class<? extends Servlet>) type)) {
                        int depth = depthOf((Class<? extends Servlet>) type);
                        if (depth > curDepth) {
                            curDepth = depth;
                            curClass = (Class<? extends Servlet>) type;
                        }
                    }
                }
            }
        }
        System.err.println(" ----> " + curClass);
        return curClass;
    }

    int depthOf(Class<?> cls) {
        int depth = 1;

        while (cls.getSuperclass() != null) {
            ++depth;
            cls = cls.getSuperclass();
        }
        return depth;
    }

    Supplier<Servlet> supplier(Bean<Servlet> servletBean, BeanManager beanManager) {
        return () -> {
            CreationalContext<Servlet> ctx = beanManager.createCreationalContext(servletBean);
            return servletBean.create(ctx);
        };
    }

    void processUrlPatterns(ServletMetaData meta, Bean<Servlet> servletBean) {
        Set<Type> types = servletBean.getTypes();
        for (Type type : types) {
            if (type instanceof Class) {
                WebServlet[] annos = (WebServlet[]) ((Class) type).getAnnotationsByType(WebServlet.class);
                for (WebServlet anno : annos) {
                    for (String pattern : anno.urlPatterns()) {
                        meta.addUrlPattern(pattern);
                    }
                }
            }
        }
    }
}
