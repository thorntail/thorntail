package org.jboss.unimbus.servlet.impl.undertow.util;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import javax.servlet.Servlet;

import io.undertow.servlet.Servlets;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.FilterInfo;
import io.undertow.servlet.api.HttpMethodSecurityInfo;
import io.undertow.servlet.api.InstanceFactory;
import io.undertow.servlet.api.InstanceHandle;
import io.undertow.servlet.api.ListenerInfo;
import io.undertow.servlet.api.LoginConfig;
import io.undertow.servlet.api.SecurityConstraint;
import io.undertow.servlet.api.SecurityInfo;
import io.undertow.servlet.api.ServletInfo;
import io.undertow.servlet.api.ServletSecurityInfo;
import io.undertow.servlet.api.TransportGuaranteeType;
import io.undertow.servlet.api.WebResourceCollection;
import org.jboss.unimbus.servlet.DeploymentMetaData;
import org.jboss.unimbus.servlet.FilterMetaData;
import org.jboss.unimbus.servlet.HttpConstraintMetaData;
import org.jboss.unimbus.servlet.HttpMethodConstraintMetaData;
import org.jboss.unimbus.servlet.SecurityConstraintMetaData;
import org.jboss.unimbus.servlet.ServletMetaData;
import org.jboss.unimbus.servlet.ServletSecurityMetaData;
import org.jboss.unimbus.servlet.WebResourceCollectionMetaData;
import org.jboss.weld.environment.servlet.Listener;

/**
 * Created by bob on 1/17/18.
 */
public class DeploymentUtils {

    private DeploymentUtils() {

    }

    public static DeploymentInfo convert(DeploymentMetaData meta) {
        DeploymentInfo info = new DeploymentInfo();
        info.addListener(new ListenerInfo(
                Listener.class
        ));

        info.setDeploymentName(meta.getName());
        info.setContextPath(meta.getContextPath());
        info.setClassLoader(new SortingClassLoader(DeploymentUtils.class.getClassLoader()));


        meta.getSecurityConstraints().forEach(e -> {
            info.addSecurityConstraint(convert(e));
        });

        info.addServlets(convert(meta.getServlets()));

        meta.getInitParams().entrySet().forEach(e -> info.addInitParameter(e.getKey(), e.getValue()));
        meta.getServletContextAttributes().entrySet().forEach(e -> info.addServletContextAttribute(e.getKey(), e.getValue()));

        if (meta.getRealm() != null && !meta.getAuthMethods().isEmpty()) {
            LoginConfig loginConfig = new LoginConfig(meta.getRealm());
            info.setLoginConfig(loginConfig);
            meta.getAuthMethods().forEach(loginConfig::addLastAuthMethod);
        }

        return info;
    }

    private static SecurityConstraint convert(SecurityConstraintMetaData meta) {
        SecurityConstraint info = new SecurityConstraint();
        info.addRolesAllowed(meta.getRolesAllowed());
        meta.getWebResourceCollections().forEach(e -> {
            info.addWebResourceCollection(convert(e));
        });
        info.setEmptyRoleSemantic(convert(meta.getEmptyRoleSemantic()));
        return info;
    }

    private static WebResourceCollection convert(WebResourceCollectionMetaData e) {
        WebResourceCollection info = new WebResourceCollection();
        info.addUrlPatterns(e.getUrlPatterns());
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

        ServletSecurityMetaData security = meta.getSecurity();
        if (security != null) {
            info.setServletSecurityInfo(convert(security));
        }

        return info;
    }

    private static ServletSecurityInfo convert(ServletSecurityMetaData security) {
        ServletSecurityInfo info = new ServletSecurityInfo();
        HttpConstraintMetaData httpConstraint = security.getHttpConstraint();
        info.setTransportGuaranteeType(convert(httpConstraint.getTransportGuarantee()));
        info.setEmptyRoleSemantic(convert(httpConstraint.getEmptyRoleSemantic()));
        info.addRolesAllowed(httpConstraint.getRolesAllowed());
        security.getHttpMethodConstraints().forEach(e -> {
            info.addHttpMethodSecurityInfo(convert(e));

        });
        return info;
    }

    private static HttpMethodSecurityInfo convert(HttpMethodConstraintMetaData meta) {
        HttpMethodSecurityInfo info = new HttpMethodSecurityInfo();
        info.setMethod(meta.getMethod());
        info.setTransportGuaranteeType(convert(meta.getTransportGuarantee()));
        info.setEmptyRoleSemantic(convert(meta.getEmptyRoleSemantic()));
        info.addRolesAllowed(meta.getRolesAllowed());
        return info;
    }

    private static SecurityInfo.EmptyRoleSemantic convert(ServletSecurityMetaData.EmptyRoleSemantic emptyRoleSemantic) {
        switch (emptyRoleSemantic) {
            case PERMIT:
                return SecurityInfo.EmptyRoleSemantic.PERMIT;
            case DENY:
                return SecurityInfo.EmptyRoleSemantic.DENY;
        }
        return SecurityInfo.EmptyRoleSemantic.PERMIT;
    }

    private static TransportGuaranteeType convert(ServletSecurityMetaData.TransportGuarantee transportGuarantee) {
        switch (transportGuarantee) {
            case NONE:
                return TransportGuaranteeType.NONE;
            case CONFIDENTIAL:
                return TransportGuaranteeType.CONFIDENTIAL;
        }
        return TransportGuaranteeType.NONE;
    }

    private static FilterInfo convert(FilterMetaData meta) {
        FilterInfo info = new FilterInfo(
                meta.getName(),
                meta.getType()
        );

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
