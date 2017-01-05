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
package org.wildfly.swarm.datasources.runtime.drivers;

import javax.inject.Singleton;

import org.jboss.modules.ModuleIdentifier;
import org.wildfly.swarm.config.datasources.DataSource;
import org.wildfly.swarm.config.datasources.JDBCDriver;
import org.wildfly.swarm.datasources.runtime.DriverInfo;

/**
 * Auto-detection for Oracle.
 *
 * @author Bob McWhirter
 */
@Singleton
public class OracleDriverInfo extends DriverInfo {
    public static final String DEFAULT_CONNECTION_URL = "jdbc:oracle:thin:@localhost:1521:test";

    public static final String DEFAULT_USER_NAME = "scott";

    public static final String DEFAULT_PASSWORD = "tiger";

    public OracleDriverInfo() {
        super("oracle", ModuleIdentifier.create("com.oracle.jdbc"), "oracle.jdbc.OracleDriver");
    }

    @Override
    protected void configureDriver(JDBCDriver driver) {
        driver.xaDatasourceClass("oracle.jdbc.xa.client.OracleXADataSource");
    }

    @Override
    protected void configureDefaultDS(DataSource datasource) {
        datasource.connectionUrl(DEFAULT_CONNECTION_URL);
        datasource.userName(DEFAULT_USER_NAME);
        datasource.password(DEFAULT_PASSWORD);
        datasource.minPoolSize(5);
        datasource.maxPoolSize(10);
        datasource.allocationRetry(1);
        datasource.preparedStatementsCacheSize(32L);
        datasource.sharePreparedStatements(true);
        datasource.useCcm(true);
    }
}
