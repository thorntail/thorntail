package org.wildfly.swarm.datasources;

import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.Assignable;

/**
 * @author Bob McWhirter
 */
public interface DatasourceArchive extends Assignable {

    DatasourceArchive datasource(Datasource ds);

}
