package io.thorntail.security.keycloak.impl;

import static io.thorntail.Info.ROOT_PACKAGE;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Priority;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.keycloak.adapters.KeycloakConfigResolver;
import org.keycloak.adapters.KeycloakDeployment;

import io.thorntail.condition.annotation.RequiredClassPresent;
import io.thorntail.events.LifecycleEvent;
import io.thorntail.servlet.DeploymentMetaData;
import io.thorntail.servlet.Deployments;

/**
 * Created by bob on 1/18/18.
 */
@ApplicationScoped
@RequiredClassPresent(ROOT_PACKAGE + ".servlet.Deployments")
public class KeycloakDeploymentsCustomizer {

    void customize(@Observes @Priority(100) LifecycleEvent.Initialize event) {
        this.deployments.stream().forEach(this::customize);
    }

    void customize(DeploymentMetaData deployment) {
        if (deployment.getAuthMethods().contains("KEYCLOAK")) {
            SecurityKeycloakMessages.MESSAGES
                .configResolverForDeployment(deployment.getName() == null ? "" : deployment.getName());
            deployment.addInitParam("keycloak.config.resolver", ConfigResolver.class.getName());
            deployment.setRealm("");
        } else {
            SecurityKeycloakMessages.MESSAGES
                .noKeycloakForDeployment(deployment.getName() == null ? "" : deployment.getName());
        }
    }

    @Inject
    Deployments deployments;

    @Inject
    @ConfigProperty(name = "keycloak.json.path", defaultValue = "keycloak.json")
    String keycloakJsonPath;

    @Inject
    @ConfigProperty(name = "keycloak.multitenancy.paths")
    Optional<Map<String, String>> keycloakMultitenancyPaths;

    @Produces
    public KeycloakConfigResolver staticResolver() {
        KeycloakDeployment dep = loadKeycloakDeployment(keycloakJsonPath);
        return dep != null ? new StaticKeycloakConfigResolver(dep) : null; 
    }
    
    @Produces
    public KeycloakConfigResolver multitenancyResolver() {
        if (keycloakMultitenancyPaths.isPresent()) {
            Map<String, KeycloakDeployment> pathDeployments = new HashMap<>();
            for (Map.Entry<String, String> entry : keycloakMultitenancyPaths.get().entrySet()) {
                KeycloakDeployment dep = loadKeycloakDeployment(entry.getKey());
                if (dep != null) {
                    pathDeployments.put(entry.getKey(), dep);
                }
            }
            return new KeycloakMultitenancyConfigResolver(pathDeployments);
        } else {
            return null;
        }
    }

    private static KeycloakDeployment loadKeycloakDeployment(String path) {
        KeycloakDeployment dep = KeycloakUtils.loadFromClasspath(path);
        if (dep != null && !path.startsWith("classpath:")) {
            dep = KeycloakUtils.loadFromFilesystem(path);
        }
        return dep;
    }
}
