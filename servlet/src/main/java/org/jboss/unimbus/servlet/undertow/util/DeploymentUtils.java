package org.jboss.unimbus.servlet.undertow.util;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import javax.servlet.Servlet;

import io.undertow.Undertow;
import io.undertow.servlet.Servlets;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.InstanceFactory;
import io.undertow.servlet.api.InstanceHandle;
import io.undertow.servlet.api.ListenerInfo;
import io.undertow.servlet.api.ServletInfo;
import org.jboss.unimbus.servlet.DeploymentMetaData;
import org.jboss.unimbus.servlet.ServletMetaData;
import org.jboss.weld.environment.servlet.Listener;
import org.jboss.weld.environment.servlet.WeldServletLifecycle;

/**
 * Created by bob on 1/17/18.
 */
public class DeploymentUtils {

    private DeploymentUtils() {

    }

    public static DeploymentInfo convert(DeploymentMetaData meta) {
        DeploymentInfo info = new DeploymentInfo();
        info.setDeploymentName(meta.getName());
        info.setContextPath(meta.getContextPath());
        info.setClassLoader(DeploymentUtils.class.getClassLoader());
        meta.getServletContextAttributes().entrySet().forEach(e -> {
            info.addServletContextAttribute(e.getKey(), e.getValue());
        });
        info.addListener(new ListenerInfo(
                Listener.class
        ));
        info.addServlets(convert(meta.getServlets()));
        return info;
    }

    public static List<ServletInfo> convert(Iterable<ServletMetaData> metas) {
        return convert(StreamSupport.stream(metas.spliterator(), false));
    }

    public static List<ServletInfo> convert(List<ServletMetaData> metas) {
        return convert(metas.stream());
    }

    public static List<ServletInfo> convert(Stream<ServletMetaData> metas) {
        return metas
                .map(DeploymentUtils::convert)
                .collect(Collectors.toList());
    }

    public static ServletInfo convert(ServletMetaData meta) {
        ServletInfo info = Servlets.servlet(
                meta.getName(),
                meta.getType(),
                instanceFactory(meta.getSupplier())
        );

        info.addMappings(meta.getUrlPatterns());
        info.setLoadOnStartup(meta.getLoadOnStartup());
        info.setAsyncSupported(meta.isAsyncSupported());

        meta.getInitParams().entrySet().forEach(e -> {
            info.addInitParam(e.getKey(), e.getValue());
        });

        return info;
    }

    static InstanceFactory<? extends Servlet> instanceFactory(Supplier<? extends Servlet> supplier) {
        return () -> new InstanceHandle<Servlet>() {
            @Override
            public Servlet getInstance() {
                return supplier.get();
            }

            @Override
            public void release() {
                // no-op;
            }
        };
    }
}
