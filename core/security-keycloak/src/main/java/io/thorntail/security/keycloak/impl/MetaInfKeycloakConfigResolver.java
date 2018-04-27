package io.thorntail.security.keycloak.impl;

import javax.enterprise.context.ApplicationScoped;

/**
 * Created by bob on 1/24/18.
 */
// TODO handle priority sorting.
@ApplicationScoped
public class MetaInfKeycloakConfigResolver extends ClasspathKeycloakConfigResolver {

    protected MetaInfKeycloakConfigResolver() {
        super("META-INF/keycloak.json");
    }
}
