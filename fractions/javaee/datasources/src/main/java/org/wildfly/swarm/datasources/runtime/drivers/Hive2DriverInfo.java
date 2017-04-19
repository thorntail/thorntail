package org.wildfly.swarm.datasources.runtime.drivers;

import org.jboss.modules.ModuleIdentifier;
import org.wildfly.swarm.config.datasources.DataSource;
import org.wildfly.swarm.datasources.runtime.DriverInfo;

/**
 * Auto-detection for Apache Hive
 *
 * @author Kylin Soong
 */
public class Hive2DriverInfo extends DriverInfo {

    public static final String DEFAULT_CONNETION_URL = "jdbc:hive2://localhost:10000/default";

    public static final String DEFAULT_USER_NAME = "sa";

    public static final String DEFAULT_PASSWORD = "sa";

    protected Hive2DriverInfo() {
        super("hive2", ModuleIdentifier.create("org.apache.hive.jdbc"), "org.apache.hive.jdbc.HiveDriver");
    }

    @Override
    protected void configureDefaultDS(DataSource datasource) {
        datasource.connectionUrl(DEFAULT_CONNETION_URL);
        datasource.userName(DEFAULT_USER_NAME);
        datasource.password(DEFAULT_PASSWORD);
    }

}
