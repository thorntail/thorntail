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
package org.wildfly.swarm.database.mysql.runtime;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.wildfly.swarm.datasources.DatasourcesFraction;
import org.wildfly.swarm.spi.api.Customizer;
import org.wildfly.swarm.spi.api.SwarmProperties;
import org.wildfly.swarm.spi.runtime.annotations.Pre;

/**
 * @author Ken Finnigan
 */
@Singleton
@Pre
public class MySQLDatasourceCustomizer implements Customizer {
    @Inject
    Instance<DatasourcesFraction> datasourcesFractionInstance;

    @Override
    public void customize() {
        if (!datasourcesFractionInstance.isUnsatisfied()) {
            String dsName = System.getProperty(SwarmProperties.DATASOURCE_NAME, "ExampleDS");
            String driverName = System.getProperty(SwarmProperties.DATABASE_DRIVER, "mysql");

            datasourcesFractionInstance.get()
                    .jdbcDriver(driverName, (d) -> {
                        d.driverClassName("com.mysql.jdbc.Driver");
                        d.xaDatasourceClass("com.mysql.jdbc.jdbc2.optional.MysqlXADataSource");
                        d.driverModuleName("com.mysql");
                    })
                    .dataSource(dsName, (ds) -> {
                        ds.driverName(driverName);
                        ds.connectionUrl(System.getProperty(SwarmProperties.DATASOURCE_CONNECTION_URL, "jdbc:mysql://localhost:3306/test"));
                        ds.userName(System.getProperty(SwarmProperties.DATASOURCE_USERNAME, "root"));
                        ds.password(System.getProperty(SwarmProperties.DATASOURCE_PASSWORD, "root"));
                    });
        }
    }
}
