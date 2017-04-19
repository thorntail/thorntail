package org.wildfly.swarm.datasources.runtime.drivers;

import javax.enterprise.context.ApplicationScoped;

import org.jboss.modules.ModuleIdentifier;
import org.wildfly.swarm.config.datasources.DataSource;
import org.wildfly.swarm.config.datasources.JDBCDriver;
import org.wildfly.swarm.datasources.runtime.DriverInfo;

/**
 * Auto-detection for Apache Derby
 *
 * @author Kylin Soong
 */
@ApplicationScoped
public class DerbyDriverInfo extends DriverInfo {

    public static final String DEFAULT_CONNECTION_URL = "jdbc:derby://localhost:1527/db";

    public static final String DEFAULT_USER_NAME = "sa";

    public static final String DEFAULT_PASSWORD = "sa";

    protected DerbyDriverInfo() {
        super("derby", ModuleIdentifier.create("org.apache.derby.jdbc"), "org.apache.derby.jdbc.ClientDriver");
    }

    @Override
    protected void configureDriver(JDBCDriver driver) {
        driver.driverXaDatasourceClassName("org.apache.derby.jdbc.ClientXADataSource");
    }

    @Override
    protected void configureDefaultDS(DataSource datasource) {
        datasource.connectionUrl(DEFAULT_CONNECTION_URL);
        datasource.userName(DEFAULT_USER_NAME);
        datasource.password(DEFAULT_PASSWORD);
    }

}
