/**
 * Copyright 2015-2017 Red Hat, Inc, and individual contributors.
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
package org.wildfly.swarm.datasources.runtime.drivers;

import javax.enterprise.context.ApplicationScoped;

import org.jboss.modules.ModuleDependencySpecBuilder;
import org.jboss.modules.ModuleSpec;
import org.wildfly.swarm.config.datasources.DataSource;
import org.wildfly.swarm.config.datasources.JDBCDriver;
import org.wildfly.swarm.datasources.runtime.DriverInfo;

/**
 * Auto-detection for SQLServer.
 *
 * @author Bob McWhirter
 */
@ApplicationScoped
public class SQLServerDriverInfo extends DriverInfo {
    public static final String DEFAULT_CONNECTION_URL = "jdbc:sqlserver://localhost:1433;DatabaseName=test";

    public static final String DEFAULT_USER_NAME = "admin";

    public static final String DEFAULT_PASSWORD = "admin";

    public SQLServerDriverInfo() {
        super("sqlserver", "com.microsoft", "com.microsoft.sqlserver.jdbc.SQLServerDriver");
    }

    @Override
    protected void configureDriver(JDBCDriver driver) {
        driver.driverXaDatasourceClassName("com.microsoft.sqlserver.jdbc.SQLServerXADataSource");
    }

    @Override
    protected void configureDefaultDS(DataSource datasource) {
        datasource.connectionUrl(DEFAULT_CONNECTION_URL);
        datasource.userName(DEFAULT_USER_NAME);
        datasource.password(DEFAULT_PASSWORD);
    }

    @Override
    protected void addModuleDependencies(ModuleSpec.Builder builder) {
        // JDBC driver for SQL Server 6.0 - 6.2 is using javax.xml.bind.DatatypeConverter
        builder.addDependency(new ModuleDependencySpecBuilder()
                .setName("javax.xml.bind.api")
                .build());
    }

}
