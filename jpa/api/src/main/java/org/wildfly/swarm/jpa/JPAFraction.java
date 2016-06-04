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
package org.wildfly.swarm.jpa;

import org.wildfly.swarm.config.JPA;
import org.wildfly.swarm.datasources.DatasourcesFraction;
import org.wildfly.swarm.spi.api.Fraction;
import org.wildfly.swarm.spi.api.SwarmProperties;
import org.wildfly.swarm.spi.api.annotations.Configuration;
import org.wildfly.swarm.spi.api.annotations.Default;

/**
 * @author Ken Finnigan
 * @author Lance Ball
 */
@Configuration(
        marshal = true,
        extension = "org.jboss.as.jpa",
        parserFactoryClassName = "org.wildfly.swarm.jpa.ParserFactory"
)
public class JPAFraction extends JPA<JPAFraction> implements Fraction {

    public JPAFraction() {
    }

    @Default
    public static JPAFraction createDefaultFraction() {
        return new JPAFraction()
                .defaultExtendedPersistenceInheritance(DefaultExtendedPersistenceInheritance.DEEP);

    }

    public JPAFraction inhibitDefaultDatasource() {
        this.inhibitDefaultDatasource = true;
        return this;
    }

    @Override
    public void initialize(Fraction.InitContext initContext) {
        if (!inhibitDefaultDatasource) {
            String dsName = System.getProperty(SwarmProperties.DATASOURCE_NAME, "ExampleDS");
            final DatasourcesFraction datasources = new DatasourcesFraction()
                    .jdbcDriver("h2", (d) -> {
                        d.driverClassName("org.h2.Driver");
                        d.xaDatasourceClass("org.h2.jdbcx.JdbcDataSource");
                        d.driverModuleName("com.h2database.h2");
                    })
                    .dataSource(dsName, (ds) -> {
                        ds.driverName(System.getProperty(SwarmProperties.DATABASE_DRIVER, "h2"));
                        ds.connectionUrl(System.getProperty(SwarmProperties.DATASOURCE_CONNECTION_URL, "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE"));
                        ds.userName(System.getProperty(SwarmProperties.DATASOURCE_USERNAME, "sa"));
                        ds.password(System.getProperty(SwarmProperties.DATASOURCE_PASSWORD, "sa"));
                    });

            initContext.fraction(datasources);
            defaultDatasource("jboss/datasources/" + dsName);
        }
    }

    private boolean inhibitDefaultDatasource = false;
}
