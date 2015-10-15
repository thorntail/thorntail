package org.wildfly.swarm.jpa;

import org.wildfly.swarm.config.Jpa;
import org.wildfly.swarm.config.datasources.DataSource;
import org.wildfly.swarm.config.datasources.JdbcDriver;
import org.wildfly.swarm.container.Container;
import org.wildfly.swarm.container.Fraction;
import org.wildfly.swarm.datasources.DatasourcesFraction;

/**
 * @author Ken Finnigan
 * @author Lance Ball
 */
public class JPAFraction extends Jpa<JPAFraction> implements Fraction {

    private boolean inhibitDefaultDatasource = false;

    public JPAFraction() {
    }

    public JPAFraction inhibitDefaultDatasource() {
        this.inhibitDefaultDatasource = true;
        return this;
    }

    @Override
    public void initialize(Container.InitContext initContext) {
        if (!inhibitDefaultDatasource) {
            final DatasourcesFraction datasources = new DatasourcesFraction()
                    .jdbcDriver(new JdbcDriver("h2")
                            .driverName("h2")
                            .driverDatasourceClassName("org.h2.driver")
                            .driverXaDatasourceClassName("org.h2.jdbcx.JdbcDataSource")
                            .driverModuleName("com.h2database.h2"))
                    .dataSource(new DataSource("ExampleDS")
                            .connectionUrl("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE")
                            .userName("sa")
                            .password("sa")
                            .driverName("h2"));

            initContext.fraction(datasources);
            System.err.println( "setting default Datasource to ExampleDS" );
            defaultDatasource("jboss/datasources/ExampleDS");
        }
    }

    public static JPAFraction createDefaultFraction() {
        return new JPAFraction()
                .defaultExtendedPersistenceInheritance("DEEP");

    }
}
