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
import org.wildfly.swarm.config.datasources.DataSource;
import org.wildfly.swarm.config.datasources.JDBCDriver;
import org.wildfly.swarm.container.Container;
import org.wildfly.swarm.container.Fraction;
import org.wildfly.swarm.datasources.DatasourcesFraction;

/**
 * @author Ken Finnigan
 * @author Lance Ball
 */
public class JPAFraction extends JPA<JPAFraction> implements Fraction {

    private boolean inhibitDefaultDatasource = false;

    public JPAFraction() {
    }

    public JPAFraction inhibitDefaultDatasource() {
        this.inhibitDefaultDatasource = true;
        return this;
    }

    @Override
    public void initialize(Container.InitContext initContext) {
        if (!inhibitDefaultDatasource) {
            final DatasourcesFraction datasources = new DatasourcesFraction()
                    .jdbcDriver(new JDBCDriver("h2")
                            .driverName("h2")
                            .driverDatasourceClassName("org.h2.driver")
                            .driverXaDatasourceClassName("org.h2.jdbcx.JdbcDataSource")
                            .driverModuleName("com.h2database.h2"))
                    .dataSource(new DataSource("ExampleDS")
                            .connectionUrl("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE")
                            .userName("sa")
                            .password("sa")
                            .driverName("h2"));

            initContext.fraction(datasources);
            System.err.println( "setting default Datasource to ExampleDS" );
            defaultDatasource("jboss/datasources/ExampleDS");
        }
    }

    public static JPAFraction createDefaultFraction() {
        return new JPAFraction()
                .defaultExtendedPersistenceInheritance(DefaultExtendedPersistenceInheritance.DEEP);

    }
}
