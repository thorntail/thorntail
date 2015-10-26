package org.wildfly.swarm.datasources;

/**
 * @author Bob McWhirter
 */
public class JdbcDriver extends org.wildfly.swarm.config.datasources.JDBCDriver<JdbcDriver> {

    public JdbcDriver(String key) {
        super(key);
        driverName(key);
    }
}
