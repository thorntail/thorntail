package io.thorntail.security.keycloak.impl;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.annotation.Priority;
import javax.enterprise.context.ApplicationScoped;

import org.keycloak.adapters.KeycloakConfigResolver;
import org.keycloak.adapters.KeycloakDeployment;
import org.keycloak.adapters.KeycloakDeploymentBuilder;
import org.keycloak.adapters.spi.HttpFacade;
import org.keycloak.representations.adapters.config.AdapterConfig;

/**
 * Created by bob on 1/24/18.
 */
public class ClasspathKeycloakConfigResolver implements KeycloakConfigResolver {

    protected ClasspathKeycloakConfigResolver(String path) {
        this.path = path;
    }

    @Override
    public KeycloakDeployment resolve(HttpFacade.Request request) {

        URL url = getClass().getClassLoader().getResource(this.path);

        if (url != null) {
            try (InputStream in = url.openStream()) {
                AdapterConfig adapterConfig = KeycloakDeploymentBuilder.loadAdapterConfig(in);
                return KeycloakDeploymentBuilder.build(adapterConfig);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private final String path;
}
