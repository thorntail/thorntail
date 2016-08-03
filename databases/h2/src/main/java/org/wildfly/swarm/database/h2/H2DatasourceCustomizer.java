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
package org.wildfly.swarm.database.h2;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.wildfly.swarm.datasources.DatasourcesFraction;
import org.wildfly.swarm.spi.api.Customizer;
import org.wildfly.swarm.spi.api.Pre;
import org.wildfly.swarm.spi.api.SwarmProperties;

/**
 * @author Ken Finnigan
 */
@Singleton
@Pre
public class H2DatasourceCustomizer implements Customizer {
    @Inject
    @Any
    Instance<DatasourcesFraction> datasourcesFractionInstance;

    @Override
    public void customize() {
        if (!datasourcesFractionInstance.isUnsatisfied()) {
            String dsName = System.getProperty(SwarmProperties.DATASOURCE_NAME, "ExampleDS");
            String driverName = System.getProperty(SwarmProperties.DATABASE_DRIVER, "h2");

            datasourcesFractionInstance.get()
                    .jdbcDriver(driverName, (d) -> {
                        d.driverClassName("org.h2.Driver");
                        d.xaDatasourceClass("org.h2.jdbcx.JdbcDataSource");
                        d.driverModuleName("com.h2database.h2");
                    })
                    .dataSource(dsName, (ds) -> {
                        ds.driverName(driverName);
                        ds.connectionUrl(System.getProperty(SwarmProperties.DATASOURCE_CONNECTION_URL, "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE"));
                        ds.userName(System.getProperty(SwarmProperties.DATASOURCE_USERNAME, "sa"));
                        ds.password(System.getProperty(SwarmProperties.DATASOURCE_PASSWORD, "sa"));
                    });
        }
    }
}
