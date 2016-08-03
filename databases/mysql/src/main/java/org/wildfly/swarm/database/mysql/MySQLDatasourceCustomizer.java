package org.wildfly.swarm.database.mysql;

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
public class MySQLDatasourceCustomizer implements Customizer {
    @Inject
    Instance<DatasourcesFraction> datasourcesFractionInstance;

    @Override
    public void customize() {
        if (!datasourcesFractionInstance.isUnsatisfied()) {
            String dsName = System.getProperty(SwarmProperties.DATASOURCE_NAME, "ExampleDS");
            String driverName = System.getProperty(SwarmProperties.DATABASE_DRIVER, "mysql");

            datasourcesFractionInstance.get()
                    .jdbcDriver(driverName, (d) -> {
                        d.driverClassName("com.mysql.jdbc.Driver");
                        d.xaDatasourceClass("com.mysql.jdbc.jdbc2.optional.MysqlXADataSource");
                        d.driverModuleName("com.mysql");
                    })
                    .dataSource(dsName, (ds) -> {
                        ds.driverName(driverName);
                        ds.connectionUrl(System.getProperty(SwarmProperties.DATASOURCE_CONNECTION_URL, "jdbc:mysql://localhost:3306/test"));
                        ds.userName(System.getProperty(SwarmProperties.DATASOURCE_USERNAME, "root"));
                        ds.password(System.getProperty(SwarmProperties.DATASOURCE_PASSWORD, "root"));
                    });
        }
    }
}
