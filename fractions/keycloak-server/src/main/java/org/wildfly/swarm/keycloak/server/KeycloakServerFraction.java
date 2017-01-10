/**
 * Copyright 2015-2016 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wildfly.swarm.keycloak.server;

import org.wildfly.swarm.config.KeycloakServer;
import org.wildfly.swarm.spi.api.Fraction;
import org.wildfly.swarm.spi.api.annotations.MarshalDMR;
import org.wildfly.swarm.spi.api.annotations.WildFlyExtension;
import org.wildfly.swarm.spi.api.annotations.WildFlySubsystem;

import static org.wildfly.swarm.keycloak.server.KeycloakServerProperties.DEFAULT_REALM_NAME;
import static org.wildfly.swarm.keycloak.server.KeycloakServerProperties.DEFAULT_WEB_CONTEXT;

/**
 * @author Bob McWhirter
 */
@WildFlyExtension(module = "org.keycloak.keycloak-server-subsystem")
@WildFlySubsystem("keycloak-server")
@MarshalDMR
public class KeycloakServerFraction extends KeycloakServer<KeycloakServerFraction> implements Fraction {

    public static final String JPA = "jpa";

    public static final String DEFAULT = "default";

    public static final String BASIC = "basic";

    public KeycloakServerFraction() {

    }

    @Override
    public Fraction applyDefaults() {
        System.err.println("Applying defaults");
        webContext(DEFAULT_WEB_CONTEXT);
        masterRealmName(DEFAULT_REALM_NAME);
        scheduledTaskInterval(900L);

        spi("eventStore", (eventStore) -> {
            eventStore.defaultProvider("jpa");
            eventStore.provider("jpa", (provider) -> {
                provider.enabled(true);
                provider.property("exclude-events", "[\"REFRESH_TOKEN\"]");
            });
        });

        spi("realm", (spi) -> {
            spi.defaultProvider(JPA);
        });

        spi("user", (spi) -> {
            spi.defaultProvider(JPA);
        });

        spi("userFederatedStorage", (spi) -> {
            spi.defaultProvider(JPA);
        });

        spi("userCache", (spi) -> {
            spi.provider(DEFAULT, (provider) -> {
                provider.enabled(true);
            });
        });

        spi("userSessionPersister", (spi) -> {
            spi.defaultProvider(JPA);
        });

        spi("authorizationPersister", (spi) -> {
            spi.defaultProvider(JPA);
        });

        spi("timer", (spi) -> {
            spi.defaultProvider(BASIC);
        });

        spi("connectionsHttpClient", (spi) -> {
            spi.provider(DEFAULT, (provider) -> {
                provider.enabled(true);
            });
        });

        spi("connectionsJpa", (spi) -> {
            spi.provider(DEFAULT, (provider) -> {
                provider.enabled(true);
                provider.property("dataSource", "java:jboss/datasources/KeycloakDS");
                provider.property("initializeEmpty", "true");
                provider.property("migrationStrategy", "update");
                provider.property("migrationExport", "${user.dir}/keycloak-database-update.sql");
            });
        });

        spi("realmCache", (spi) -> {
            spi.provider(DEFAULT, (provider) -> {
                provider.enabled(true);
            });
        });

        spi("connectionsInfinispan", (spi) -> {
            spi.defaultProvider(DEFAULT);
            spi.provider(DEFAULT, (provider) -> {
                provider.enabled(true);
                provider.property("cacheContainer", "java:comp/env/infinispan/Keycloak");
            });
        });

        spi("jta-lookup", (spi) -> {
            spi.defaultProvider("${keycloak.jta.lookup.provider:jboss}");
            spi.provider("jboss", (provider) -> {
                provider.enabled(true);
            });
        });

        spi("publicKeyStorage", (spi) -> {
            spi.provider("infinispan", (provider) -> {
                provider.enabled(true);
                provider.property("minTimeBetweenRequests", "10");
            });
        });


        return this;
    }
}
