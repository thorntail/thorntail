package org.wildfly.swarm.datasources;

import org.wildfly.swarm.config.datasources.Datasources;
import org.wildfly.swarm.config.datasources.subsystem.dataSource.DataSource;
import org.wildfly.swarm.container.Fraction;

import java.util.List;

/**
 * @author Bob McWhirter
 */
public class DatasourcesFraction extends Datasources implements Fraction {
    @Override
    public Datasources dataSource(DataSource value) {
        if (value.jndiName() == null) value.jndiName("java:jboss/datasources/" + value.getKey());
        return super.dataSource(value);
    }
}
