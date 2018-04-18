package io.thorntail.security.keycloak.impl;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.enterprise.context.ApplicationScoped;

import org.keycloak.adapters.KeycloakConfigResolver;
import org.keycloak.adapters.KeycloakDeployment;
import org.keycloak.adapters.KeycloakDeploymentBuilder;
import org.keycloak.adapters.spi.HttpFacade;
import org.keycloak.representations.adapters.config.AdapterConfig;

/**
 * Created by bob on 1/24/18.
 */
// TODO handle priority sorting
@ApplicationScoped
public class FilesystemKeycloakConfigResolver implements KeycloakConfigResolver {

    @Override
    public KeycloakDeployment resolve(HttpFacade.Request facade) {
        Path path = Paths.get("keycloak.json");
        if (Files.exists(path)) {
            try ( InputStream in = new FileInputStream(path.toFile())) {
                AdapterConfig adapterConfig = KeycloakDeploymentBuilder.loadAdapterConfig(in);
                return KeycloakDeploymentBuilder.build(adapterConfig);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
