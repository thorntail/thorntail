package org.wildfly.swarm.datasources;

/**
 * @author Bob McWhirter
 */
public class JDBCDriver extends org.wildfly.swarm.config.datasources.JDBCDriver<JDBCDriver> {

    public JDBCDriver(String key) {
        super(key);
        driverName(key);
    }
}
