package io.thorntail.servlet.ext;

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
import javax.servlet.annotation.ServletSecurity;
import javax.servlet.annotation.WebServlet;

import io.thorntail.servlet.ServletMetaData;
import io.thorntail.servlet.ServletSecurityMetaData;

/**
 * Created by bob on 1/17/18.
 */
public class ServletExtension implements Extension {

    void createServletMetaData(@Observes AfterBeanDiscovery event, BeanManager beanManager) {
        beanManager.getBeans(Servlet.class).forEach(e -> {
            createServletMetaData((Bean<Servlet>) e, event, beanManager);
        });
    }

    private void createServletMetaData(Bean<Servlet> servletBean, AfterBeanDiscovery event, BeanManager beanManager) {
        ServletMetaData meta = new ServletMetaData(getType(servletBean), supplier(servletBean, beanManager));

        processUrlPatterns(meta, servletBean);
        processSecurity(meta, servletBean);

        event.addBean()
                .scope(Dependent.class)
                .addQualifier(Default.Literal.INSTANCE)
                .addType(Object.class)
                .addType(ServletMetaData.class)
                .produceWith((obj) -> meta);
    }

    private Class<? extends Servlet> getType(Bean<Servlet> servletBean) {
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
        return curClass;
    }

    private int depthOf(Class<?> cls) {
        int depth = 1;

        while (cls.getSuperclass() != null) {
            ++depth;
            cls = cls.getSuperclass();
        }
        return depth;
    }

    private Supplier<Servlet> supplier(Bean<Servlet> servletBean, BeanManager beanManager) {
        return () -> {
            CreationalContext<Servlet> ctx = beanManager.createCreationalContext(servletBean);
            return servletBean.create(ctx);
        };
    }

    private void processUrlPatterns(ServletMetaData meta, Bean<Servlet> servletBean) {
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

    private void processSecurity(ServletMetaData meta, Bean<Servlet> servletBean) {
        Set<Type> types = servletBean.getTypes();
        for (Type type : types) {
            if (type instanceof Class) {
                ServletSecurity anno = (ServletSecurity) ((Class) type).getAnnotation(ServletSecurity.class);
                if (anno != null) {
                    meta.setSecurity(new ServletSecurityMetaData(anno));
                    break;
                }
            }
        }
    }
}
