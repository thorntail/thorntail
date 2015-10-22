package org.wildfly.swarm.datasources;

/**
 * @author Bob McWhirter
 */
public class JdbcDriver extends org.wildfly.swarm.config.datasources.JdbcDriver<JdbcDriver> {

    public JdbcDriver(String key) {
        super(key);
        driverName(key);
    }
}
