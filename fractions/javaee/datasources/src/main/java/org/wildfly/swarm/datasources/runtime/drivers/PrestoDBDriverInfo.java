package org.wildfly.swarm.datasources.runtime.drivers;

import org.jboss.modules.ModuleIdentifier;
import org.wildfly.swarm.config.datasources.DataSource;
import org.wildfly.swarm.datasources.runtime.DriverInfo;

/**
 * Auto-detection for PrestoDB
 *
 * @author Kylin Soong
 */
public class PrestoDBDriverInfo extends DriverInfo {

    public static final String DEFAULT_CONNETION_URL = "jdbc:presto://localhost:8080";

    public static final String DEFAULT_USER_NAME = "sa";

    public static final String DEFAULT_PASSWORD = "sa";

    protected PrestoDBDriverInfo() {
        super("prestodb", ModuleIdentifier.create("com.facebook.presto.jdbc"), "com.facebook.presto.jdbc.PrestoDriver");
    }

    @Override
    protected void configureDefaultDS(DataSource datasource) {
        datasource.connectionUrl(DEFAULT_CONNETION_URL);
        datasource.userName(DEFAULT_USER_NAME);
        datasource.password(DEFAULT_PASSWORD);
    }

}
