package org.wildfly.swarm.datasources;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * @author Ken Finnigan
 */
@Singleton
public class DatasourceNameProducer {
    @Inject
    Instance<DatasourcesFraction> datasourcesFractionInstance;

    @Produces
    @Dependent
    @DefaultDatasource
    public String getDatasourceName() {
        if (!datasourcesFractionInstance.isUnsatisfied()) {
            return datasourcesFractionInstance.get().subresources().dataSources().get(0).getKey();
        }

        return null;
    }
}
