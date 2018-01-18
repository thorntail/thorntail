package org.jboss.unimbus.security.undertow;

import java.util.Collections;
import java.util.List;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
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
import io.undertow.servlet.api.ServletInfo;
import org.jboss.unimbus.security.basic.BasicSecurity;
import org.jboss.weld.environment.servlet.WeldServletLifecycle;

/**
 * Created by bob on 1/18/18.
 */
public class SimpleAuthServletExtension implements ServletExtension {
    private final static String AUTH_MECH_NAME = "BASIC";

    @Override
    public void handleDeployment(DeploymentInfo deploymentInfo, ServletContext servletContext) {
        System.err.println("HANDLE DEPLOYMENT: " + deploymentInfo);
        if (!deploymentInfo.isAuthenticationMechanismPresent(AUTH_MECH_NAME)) {
            return;
        }
        System.err.println("APPLY SIMPLE AUTH to " + deploymentInfo.getDeploymentName());

        for (ServletInfo each : deploymentInfo.getServlets().values()) {
            System.err.println("empty: " + each.getServletSecurityInfo().getEmptyRoleSemantic());
        }

        deploymentInfo.setSecurityDisabled(true);

        BeanManager beanManager = (BeanManager) deploymentInfo.getServletContextAttributes().get(WeldServletLifecycle.BEAN_MANAGER_ATTRIBUTE_NAME);
        //Bean<BasicIdentityManager> bean = (Bean<BasicIdentityManager>) beanManager.resolve(beanManager.getBeans(BasicIdentityManager.class));
        //CreationalContext<BasicIdentityManager> context = beanManager.createCreationalContext(bean);
        //BasicIdentityManager identityManager = bean.create(context);

        IdentityManager idm = new ProxyIdentityManager(beanManager);

        BasicAuthenticationMechanism basicAuthMech = new BasicAuthenticationMechanism(
                deploymentInfo.getLoginConfig().getRealmName(),
                AUTH_MECH_NAME,
                false,
                idm
        );

        System.err.println( "basic auth mech: " + basicAuthMech );

        deploymentInfo.addInitialHandlerChainWrapper((toWrap) -> {
            HttpHandler handler = toWrap;
            handler = new AuthenticationCallHandler(handler);
            handler = new AuthenticationConstraintHandler(handler);
            final List<AuthenticationMechanism> mechanisms = Collections.singletonList(basicAuthMech);
            handler = new AuthenticationMechanismsHandler(handler, mechanisms);
            handler = new SecurityInitialHandler(AuthenticationMode.PRO_ACTIVE, idm, handler);
            return handler;
        });

        /*
        deploymentInfo.addAuthenticationMechanism(AUTH_MECH_NAME,
                                                  (mechanismName, formParserFactory, properties) ->
                                                          //new SimpleAuthenticationMechanism(security, deploymentInfo.getLoginConfig().getRealmName()));
                                                          new BasicAuthenticationMechanism(
                                                                  deploymentInfo.getLoginConfig().getRealmName(),
                                                                  AUTH_MECH_NAME,
                                                                  false,
                                                                  identityManager
                                                          ));
                                                          */
    }

}
