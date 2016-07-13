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
package org.wildfly.swarm.jpa.postgresql;

import org.wildfly.swarm.config.JPA;
import org.wildfly.swarm.datasources.DatasourcesFraction;
import org.wildfly.swarm.spi.api.Fraction;
import org.wildfly.swarm.spi.api.SwarmProperties;
import org.wildfly.swarm.spi.api.annotations.Default;
import org.wildfly.swarm.spi.api.annotations.ExtensionModule;
import org.wildfly.swarm.spi.api.annotations.MarshalDMR;

@ExtensionModule("org.jboss.as.jpa")
@MarshalDMR
public class PostgreSQLJPAFraction extends JPA<PostgreSQLJPAFraction> implements Fraction {

    public PostgreSQLJPAFraction() {
    }

    @Default
    public static PostgreSQLJPAFraction createDefaultFraction() {
        return new PostgreSQLJPAFraction()
                .defaultExtendedPersistenceInheritance(DefaultExtendedPersistenceInheritance.DEEP);
    }

    public PostgreSQLJPAFraction inhibitDefaultDatasource() {
        this.inhibitDefaultDatasource = true;
        return this;
    }

    @Override
    public void initialize(Fraction.InitContext initContext) {
        if (!inhibitDefaultDatasource) {
            String dsName = System.getProperty(SwarmProperties.DATASOURCE_NAME, "ExampleDS");
            String driverName = System.getProperty(SwarmProperties.DATABASE_DRIVER, "postgresql");
            final DatasourcesFraction datasources = new DatasourcesFraction()
                    .jdbcDriver(driverName, (d) -> {
                        d.driverClassName("org.postgresql.Driver");
                        d.xaDatasourceClass("org.postgresql.xa.PGXADataSource");
                        d.driverModuleName("org.postgresql");
                    })
                    .dataSource(dsName, (ds) -> {
                        ds.driverName(driverName);
                        ds.connectionUrl(System.getProperty(SwarmProperties.DATASOURCE_CONNECTION_URL, "jdbc:postgresql://localhost:5432/test"));
                        ds.userName(System.getProperty(SwarmProperties.DATASOURCE_USERNAME, "postgres"));
                        ds.password(System.getProperty(SwarmProperties.DATASOURCE_PASSWORD, "postgres"));
                    });

            initContext.fraction(datasources);
            System.err.println("setting default Datasource to ExampleDS");
            defaultDatasource("jboss/datasources/" + dsName);
        }
    }

    private boolean inhibitDefaultDatasource = false;
}
