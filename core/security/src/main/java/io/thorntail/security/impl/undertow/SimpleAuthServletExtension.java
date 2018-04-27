package io.thorntail.security.impl.undertow;

import java.util.Collections;
import java.util.List;

import javax.enterprise.inject.spi.BeanManager;
import javax.servlet.ServletContext;

import io.undertow.security.api.AuthenticationMechanism;
import io.undertow.security.api.AuthenticationMode;
import io.undertow.security.handlers.AuthenticationCallHandler;
import io.undertow.security.handlers.AuthenticationConstraintHandler;
import io.undertow.security.handlers.AuthenticationMechanismsHandler;
import io.undertow.security.handlers.SecurityInitialHandler;
import io.undertow.security.idm.IdentityManager;
import io.undertow.security.impl.BasicAuthenticationMechanism;
import io.undertow.server.HttpHandler;
import io.undertow.servlet.ServletExtension;
import io.undertow.servlet.api.DeploymentInfo;
import org.jboss.weld.environment.servlet.WeldServletLifecycle;

/**
 * Created by bob on 1/18/18.
 */
public class SimpleAuthServletExtension implements ServletExtension {
    private final static String AUTH_MECH_NAME = "BASIC";

    @Override
    public void handleDeployment(DeploymentInfo deploymentInfo, ServletContext servletContext) {
        if (!deploymentInfo.isAuthenticationMechanismPresent(AUTH_MECH_NAME)) {
            return;
        }

        deploymentInfo.setSecurityDisabled(true);

        BeanManager beanManager = (BeanManager) deploymentInfo.getServletContextAttributes().get(WeldServletLifecycle.BEAN_MANAGER_ATTRIBUTE_NAME);

        IdentityManager idm = new ProxyIdentityManager(beanManager);

        BasicAuthenticationMechanism basicAuthMech = new BasicAuthenticationMechanism(
                deploymentInfo.getLoginConfig().getRealmName(),
                AUTH_MECH_NAME,
                false,
                idm
        );

        deploymentInfo.addInitialHandlerChainWrapper((toWrap) -> {
            HttpHandler handler = toWrap;
            handler = new AuthenticationCallHandler(handler);
            handler = new AuthenticationConstraintHandler(handler);
            final List<AuthenticationMechanism> mechanisms = Collections.singletonList(basicAuthMech);
            handler = new AuthenticationMechanismsHandler(handler, mechanisms);
            handler = new SecurityInitialHandler(AuthenticationMode.PRO_ACTIVE, idm, handler);
            return handler;
        });
    }
}
