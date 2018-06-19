package io.thorntail.security.keycloak.impl;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.keycloak.adapters.KeycloakDeployment;
import org.keycloak.adapters.KeycloakDeploymentBuilder;
import org.keycloak.representations.adapters.config.AdapterConfig;

public final class KeycloakUtils {
    private KeycloakUtils() {
    }
    
    public static KeycloakDeployment loadFromClasspath(String path) {
        if (path == null) {
            return null;
        }
        if (path.startsWith("classpath:")) {
            path = path.substring("classpath:".length());
        }
        URL url = KeycloakUtils.class.getClassLoader().getResource(path);
        if (url == null) {
            url = KeycloakUtils.class.getClassLoader().getResource("META-INF/" + path);
        }
        if (url == null) {
            url = KeycloakUtils.class.getClassLoader().getResource("WEB-INF/" + path);
        }
        if (url == null) {
            url = KeycloakUtils.class.getClassLoader().getResource("WEB-INF/classes" + path);
        }
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
    public static KeycloakDeployment loadFromFilesystem(String keycloakJsonPath) {
        if (keycloakJsonPath == null) {
            return null;
        }
        Path path = Paths.get(keycloakJsonPath);
        if (Files.exists(path)) {
            try (InputStream in = new FileInputStream(path.toFile())) {
                AdapterConfig adapterConfig = KeycloakDeploymentBuilder.loadAdapterConfig(in);
                return KeycloakDeploymentBuilder.build(adapterConfig);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
