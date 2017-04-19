package org.wildfly.swarm.datasources.runtime.drivers;

import javax.enterprise.context.ApplicationScoped;

import org.jboss.modules.ModuleIdentifier;
import org.wildfly.swarm.config.datasources.DataSource;
import org.wildfly.swarm.config.datasources.JDBCDriver;
import org.wildfly.swarm.datasources.runtime.DriverInfo;

/**
 * Auto-detection for MariaDB
 *
 * @author Kylin Soong
 */
@ApplicationScoped
public class MariaDBDriverInfo extends DriverInfo {

    public static final String DEFAULT_CONNECTION_URL = "jdbc:mariadb://localhost:3306/test";

    public static final String DEFAULT_USER_NAME = "root";

    public static final String DEFAULT_PASSWORD = "root";

    protected MariaDBDriverInfo() {
        super("mariadb", ModuleIdentifier.create("org.mariadb.jdbc"), "org.mariadb.jdbc.Driver");
    }

    @Override
    protected void configureDriver(JDBCDriver driver) {
        driver.driverXaDatasourceClassName("org.mariadb.jdbc.MariaDbDataSource");
    }

    @Override
    protected void configureDefaultDS(DataSource datasource) {
        datasource.connectionUrl(DEFAULT_CONNECTION_URL);
        datasource.userName(DEFAULT_USER_NAME);
        datasource.password(DEFAULT_PASSWORD);
    }

}
