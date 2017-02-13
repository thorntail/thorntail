package org.wildfly.swarm.jolokia.runtime;

import java.util.function.Consumer;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.jboss.shrinkwrap.api.Archive;
import org.wildfly.swarm.jolokia.JolokiaFraction;
import org.wildfly.swarm.jolokia.JolokiaProperties;
import org.wildfly.swarm.keycloak.KeycloakFraction;
import org.wildfly.swarm.keycloak.Secured;
import org.wildfly.swarm.spi.api.Customizer;
import org.wildfly.swarm.spi.runtime.annotations.ConfigurationValue;
import org.wildfly.swarm.spi.runtime.annotations.Pre;

/**
 * @author Bob McWhirter
 */
@Pre
@ApplicationScoped
public class JolokiaKeycloakCustomizer implements Customizer {

    @Inject
    KeycloakFraction keycloak;

    @Inject
    JolokiaFraction jolokia;

    @Inject
    @ConfigurationValue(JolokiaProperties.KEYCLOAK_ROLE)
    String role;

    @Override
    public void customize() {
        if (this.role == null) {
            return;
        }

        Consumer<Archive> keycloakPreparer = (archive) -> {
            archive.as(Secured.class)
                    .protect()
                    .withRole(this.role);
        };

        Consumer<Archive> preparer = this.jolokia.jolokiaWarPreparer();

        if (preparer == null) {
            preparer = keycloakPreparer;
        } else {
            preparer = preparer.andThen(keycloakPreparer);
        }

        this.jolokia.prepareJolokiaWar(preparer);
    }
}
