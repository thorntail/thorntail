package org.wildfly.swarm.datasources;

import org.wildfly.swarm.config.datasources.Datasources;
import org.wildfly.swarm.config.datasources.subsystem.dataSource.DataSource;
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
}
