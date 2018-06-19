package io.thorntail.security.keycloak.impl;

import org.keycloak.adapters.KeycloakConfigResolver;
import org.keycloak.adapters.KeycloakDeployment;
import org.keycloak.adapters.spi.HttpFacade;

/**
 * Created by bob on 1/24/18.
 */
public class FilesystemKeycloakConfigResolver implements KeycloakConfigResolver {

    private String path;

    public FilesystemKeycloakConfigResolver(String path) {
        this.path = path;
    }

    @Override
    public KeycloakDeployment resolve(HttpFacade.Request facade) {
        return KeycloakUtils.loadFromFilesystem(path);
    }
}
