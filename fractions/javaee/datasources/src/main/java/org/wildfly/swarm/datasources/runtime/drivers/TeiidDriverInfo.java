package org.wildfly.swarm.datasources.runtime.drivers;

import javax.enterprise.context.ApplicationScoped;

import org.jboss.modules.ModuleIdentifier;
import org.wildfly.swarm.config.datasources.DataSource;
import org.wildfly.swarm.config.datasources.JDBCDriver;
import org.wildfly.swarm.datasources.runtime.DriverInfo;

/**
 * Auto-detection for Teiid
 *
 * @author Kylin Soong
 */
@ApplicationScoped
public class TeiidDriverInfo extends DriverInfo {

    public static final String DEFAULT_CONNECTION_URL = "jdbc:teiid:Portfolio@mm://localhost:31000;version=1";

    public static final String DEFAULT_USER_NAME = "teiidUser";

    public static final String DEFAULT_PASSWORD = "password1!";

    protected TeiidDriverInfo() {
        super("teiid", ModuleIdentifier.create("org.teiid.jdbc"), "org.teiid.jdbc.TeiidDriver", "org.teiid.core.types.JDBCSQLTypeInfo");
    }

    @Override
    protected void configureDriver(JDBCDriver driver) {
        driver.driverXaDatasourceClassName("org.teiid.jdbc.TeiidDataSource");
    }

    @Override
    protected void configureDefaultDS(DataSource datasource) {
        datasource.connectionUrl(DEFAULT_CONNECTION_URL);
        datasource.userName(DEFAULT_USER_NAME);
        datasource.password(DEFAULT_PASSWORD);
    }

}
