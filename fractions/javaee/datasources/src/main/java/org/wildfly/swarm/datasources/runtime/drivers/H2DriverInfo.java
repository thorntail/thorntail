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

import javax.enterprise.context.ApplicationScoped;

import org.jboss.modules.ModuleIdentifier;
import org.wildfly.swarm.config.datasources.DataSource;
import org.wildfly.swarm.config.datasources.JDBCDriver;
import org.wildfly.swarm.datasources.runtime.DriverInfo;

/**
 * Auto-detection for H2.
 *
 * @author Bob McWhirter
 */
@ApplicationScoped
public class H2DriverInfo extends DriverInfo {

    public static final String DEFAULT_CONNETION_URL = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE";

    public static final String DEFAULT_USER_NAME = "sa";

    public static final String DEFAULT_PASSWORD = "sa";

    public H2DriverInfo() {
        super("h2", ModuleIdentifier.create("com.h2database.h2"), "org.h2.Driver");
    }

    @Override
    protected void configureDriver(JDBCDriver driver) {
        driver.driverXaDatasourceClassName("org.h2.jdbcx.JdbcDataSource");
    }

    @Override
    protected void configureDefaultDS(DataSource datasource) {
        datasource.connectionUrl(DEFAULT_CONNETION_URL);
        datasource.userName(DEFAULT_USER_NAME);
        datasource.password(DEFAULT_PASSWORD);
    }
}


