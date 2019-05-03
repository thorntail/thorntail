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

import org.wildfly.swarm.config.datasources.DataSource;
import org.wildfly.swarm.config.datasources.JDBCDriver;
import org.wildfly.swarm.datasources.runtime.DriverInfo;

/**
 * Auto-detection for DB2.
 *
 * @author Bob McWhirter
 */
@ApplicationScoped
public class DB2DriverInfo extends DriverInfo {

    public static final String DEFAULT_CONNECTION_URL = "jdbc:db2:ibmdb2db";

    public static final String DEFAULT_USER_NAME = "admin";

    public static final String DEFAULT_PASSWORD = "admin";

    public DB2DriverInfo() {
        super("ibmdb2", "com.ibm.db2jcc", "com.ibm.db2.jcc.DB2Driver",
              "com.ibm.db2.jcc.licenses.DB2J",
              "com.ibm.db2.jcc.licenses.DB2SQLDS",
              "com.ibm.db2.jcc.licenses.DB2UW",
              "com.ibm.db2.jcc.licenses.DB2iSeries",
              "com.ibm.db2.jcc.licenses.DB2zOS");
    }

    @Override
    protected void configureDriver(JDBCDriver driver) {
        driver.driverXaDatasourceClassName("com.ibm.db2.jcc.DB2XADataSource");
    }

    @Override
    protected void configureDefaultDS(DataSource datasource) {
        datasource.connectionUrl(DEFAULT_CONNECTION_URL);
        datasource.userName(DEFAULT_USER_NAME);
        datasource.password(DEFAULT_PASSWORD);
        datasource.minPoolSize(0);
        datasource.maxPoolSize(50);
    }
}

