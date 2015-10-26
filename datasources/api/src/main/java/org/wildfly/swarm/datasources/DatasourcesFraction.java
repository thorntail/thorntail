package org.wildfly.swarm.datasources;

import org.wildfly.swarm.config.Datasources;
import org.wildfly.swarm.config.datasources.DataSource;
import org.wildfly.swarm.config.datasources.JDBCDriverConfigurator;
import org.wildfly.swarm.container.Fraction;

/**
 * @author Bob McWhirter
 */
public class DatasourcesFraction extends Datasources<DatasourcesFraction> implements Fraction {
    @Override
    public DatasourcesFraction dataSource(DataSource value) {
        if (value.jndiName() == null) value.jndiName("java:jboss/datasources/" + value.getKey());
        return super.dataSource(value);
    }

    public DatasourcesFraction jdbcDriver(String childKey, EnhancedJDBCDriverConfigurator config) {
        JDBCDriver driver = new JDBCDriver(childKey);
        config.configure(driver);
        jdbcDriver( driver );
        return this;
    }

    public interface EnhancedJDBCDriverConfigurator extends JDBCDriverConfigurator {

    }
}
