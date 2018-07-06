package io.thorntail.security.keycloak.impl;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.CDI;

import org.keycloak.adapters.KeycloakConfigResolver;
import org.keycloak.adapters.KeycloakDeployment;
import org.keycloak.adapters.spi.HttpFacade;

/**
 * Created by bob on 1/24/18.
 */
public class ConfigResolver implements KeycloakConfigResolver {

    @Override
    public KeycloakDeployment resolve(HttpFacade.Request request) {
        for (KeycloakConfigResolver resolver : resolvers() ) {
            KeycloakDeployment config = resolver.resolve(request);
            if ( config != null ) {
                return config;
            }
        }

        return null;
    }

    private Instance<KeycloakConfigResolver> resolvers() {
        return CDI.current().getBeanManager().createInstance().select(KeycloakConfigResolver.class);
    }


}
