package org.wildfly.swarm.database.h2;

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
public class H2DatasourceCustomizer implements Customizer {
    @Inject
    Instance<DatasourcesFraction> datasourcesFractionInstance;

    @Override
    public void customize() {
        if (!datasourcesFractionInstance.isUnsatisfied()) {
            String dsName = System.getProperty(SwarmProperties.DATASOURCE_NAME, "ExampleDS");
            String driverName = System.getProperty(SwarmProperties.DATABASE_DRIVER, "h2");

            datasourcesFractionInstance.get()
                    .jdbcDriver(driverName, (d) -> {
                        d.driverClassName("org.h2.Driver");
                        d.xaDatasourceClass("org.h2.jdbcx.JdbcDataSource");
                        d.driverModuleName("com.h2database.h2");
                    })
                    .dataSource(dsName, (ds) -> {
                        ds.driverName(driverName);
                        ds.connectionUrl(System.getProperty(SwarmProperties.DATASOURCE_CONNECTION_URL, "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE"));
                        ds.userName(System.getProperty(SwarmProperties.DATASOURCE_USERNAME, "sa"));
                        ds.password(System.getProperty(SwarmProperties.DATASOURCE_PASSWORD, "sa"));
                    });
        }
    }
}
