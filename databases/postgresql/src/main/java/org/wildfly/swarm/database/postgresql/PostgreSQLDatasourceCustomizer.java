package org.wildfly.swarm.database.postgresql;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.wildfly.swarm.datasources.DatasourcesFraction;
import org.wildfly.swarm.spi.api.Customizer;
import org.wildfly.swarm.spi.api.Pre;
import org.wildfly.swarm.spi.api.SwarmProperties;

/**
 * @author Ken Finnigan
 */
@Singleton
@Pre
public class PostgreSQLDatasourceCustomizer implements Customizer {
    @Inject
    Instance<DatasourcesFraction> datasourcesFractionInstance;

    @Override
    public void customize() {
        if (!datasourcesFractionInstance.isUnsatisfied()) {
            String dsName = System.getProperty(SwarmProperties.DATASOURCE_NAME, "ExampleDS");
            String driverName = System.getProperty(SwarmProperties.DATABASE_DRIVER, "postgresql");

            datasourcesFractionInstance.get()
                    .jdbcDriver(driverName, (d) -> {
                        d.driverClassName("org.postgresql.Driver");
                        d.xaDatasourceClass("org.postgresql.xa.PGXADataSource");
                        d.driverModuleName("org.postgresql");
                    })
                    .dataSource(dsName, (ds) -> {
                        ds.driverName(driverName);
                        ds.connectionUrl(System.getProperty(SwarmProperties.DATASOURCE_CONNECTION_URL, "jdbc:postgresql://localhost:5432/test"));
                        ds.userName(System.getProperty(SwarmProperties.DATASOURCE_USERNAME, "postgres"));
                        ds.password(System.getProperty(SwarmProperties.DATASOURCE_PASSWORD, "postgres"));
                    });
        }
    }
}
