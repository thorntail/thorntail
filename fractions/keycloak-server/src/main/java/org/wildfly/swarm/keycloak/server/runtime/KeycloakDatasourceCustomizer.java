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
package org.wildfly.swarm.keycloak.server.runtime;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.inject.Inject;

import org.wildfly.swarm.datasources.DatasourcesFraction;
import org.wildfly.swarm.spi.api.Customizer;
import org.wildfly.swarm.spi.runtime.annotations.Post;

/**
 * @author Bob McWhirter
 */
@Post
@ApplicationScoped
public class KeycloakDatasourceCustomizer implements Customizer {

    @Inject
    @Any
    private DatasourcesFraction datasources;

    @Override
    public void customize() {

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
                ds.connectionUrl("jdbc:h2:${thorntail.keycloak.server.db:./keycloak};AUTO_SERVER=TRUE");
                ds.driverName("h2");
                ds.userName("sa");
                ds.password("sa");
            });
        }

    }
}
