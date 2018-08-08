package org.wildfly.swarm.datasources.runtime.drivers;

import org.wildfly.swarm.config.datasources.DataSource;
import org.wildfly.swarm.config.datasources.JDBCDriver;
import org.wildfly.swarm.datasources.runtime.DriverInfo;

/**
 * Auto detection for Apache Derby Embedded
 *
 * @author Ken Finnigan
 */
public class DerbyEmbeddedDriverInfo extends DriverInfo {

    public static final String DEFAULT_CONNECTION_URL = "jdbc:derby:db";

    public static final String DEFAULT_USER_NAME = "sa";

    public static final String DEFAULT_PASSWORD = "sa";

    protected DerbyEmbeddedDriverInfo() {
        super("derby-embedded", "org.apache.derby.embedded.jdbc", "org.apache.derby.jdbc.EmbeddedDriver");
    }

    @Override
    protected void configureDriver(JDBCDriver driver) {
        driver.driverXaDatasourceClassName("org.apache.derby.jdbc.EmbeddedXADataSource");
    }

    @Override
    protected void configureDefaultDS(DataSource datasource) {
        datasource.connectionUrl(DEFAULT_CONNECTION_URL);
        datasource.userName(DEFAULT_USER_NAME);
        datasource.password(DEFAULT_PASSWORD);
    }
}
