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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import org.wildfly.swarm.bootstrap.util.TempFileManager;
import org.wildfly.swarm.config.infinispan.CacheContainer;
import org.wildfly.swarm.container.Container;
import org.wildfly.swarm.container.Fraction;
import org.wildfly.swarm.datasources.DatasourcesFraction;
import org.wildfly.swarm.infinispan.InfinispanFraction;

/**
 * @author Bob McWhirter
 */
public class KeycloakServerFraction implements Fraction {

    public KeycloakServerFraction() {
    }

    @Override
    public void postInitialize(Container.PostInitContext initContext) {

        if (System.getProperty("jboss.server.config.dir") == null) {
            try {
                //Path dir = Files.createTempDirectory("swarm-keycloak-config");
                File dir = TempFileManager.INSTANCE.newTempDirectory("swarm-keycloak-config", ".d");
                System.setProperty("jboss.server.config.dir", dir.getAbsolutePath());
                Files.copy(getClass().getClassLoader().getResourceAsStream("keycloak-server.json"),
                           dir.toPath().resolve("keycloak-server.json"),
                           StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        InfinispanFraction infinispan = (InfinispanFraction) initContext.fraction("infinispan");

        CacheContainer cache = infinispan.subresources().cacheContainer("keycloak");
        if (cache == null) {
            infinispan.cacheContainer("keycloak", (c) -> c.jndiName("infinispan/Keycloak")
                    .localCache("realms")
                    .localCache("users")
                    .localCache("sessions")
                    .localCache("offlineSessions")
                    .localCache("loginFailures"));
        }

        DatasourcesFraction datasources = (DatasourcesFraction) initContext.fraction("datasources");

        if (datasources.subresources().dataSource("KeycloakDS") == null) {
            if (datasources.subresources().jdbcDriver("h2") == null) {
                datasources.jdbcDriver("h2", (driver) -> {
                    driver.driverModuleName("com.h2database.h2");
                    driver.moduleSlot("main");
                    driver.xaDatasourceClass("org.h2.jdbcx.JdbcDataSource");
                });
            }

            datasources.dataSource("KeycloakDS", (ds) -> {
                ds.jndiName("java:jboss/datasources/KeycloakDS");
                ds.useJavaContext(true);
                ds.connectionUrl("jdbc:h2:${wildfly.swarm.keycloak.server.db:./keycloak};AUTO_SERVER=TRUE");
                ds.driverName("h2");
                ds.userName("sa");
                ds.password("sa");
            });
        }
    }

}
