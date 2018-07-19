package io.thorntail.jaxrs.impl.opentracing.servlet;

import io.opentracing.contrib.jaxrs2.server.SpanFinishingFilter;
import io.smallrye.opentracing.SmallRyeTracingDynamicFeature;
import java.util.EnumSet;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import io.opentracing.Tracer;

/**
 * @author Pavol Loffay
 */
@Dependent
@WebListener
public class OpenTracingContextInitializer implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        ServletContext servletContext = servletContextEvent.getServletContext();
        /**
         *  TODO this does not work, it would be cleaner, the workaround is
         * {@link io.thorntail.jaxrs.impl.opentracing.jaxrs.SmallRyeDynamicFeatureWrapper}
         */
//        servletContext.setInitParameter("resteasy.providers", SmallRyeTracingDynamicFeature.class.getName());

        FilterRegistration.Dynamic filterRegistration = servletContext
                .addFilter("tracingFilter", new SpanFinishingFilter(tracer));
        filterRegistration.setAsyncSupported(true);
        filterRegistration.addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST), false, "*");
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
    }

    @Inject
    private Tracer tracer;
}
