package org.wildfly.swarm.datasources;

import org.jboss.shrinkwrap.api.Assignable;
import org.wildfly.swarm.config.datasources.DataSource;

/**
 * @author Bob McWhirter
 */
public interface DatasourceArchive extends Assignable {

    DatasourceArchive datasource(DataSource ds);

}
