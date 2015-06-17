package org.wildfly.swarm.jpa;

import org.wildfly.swarm.container.Container;
import org.wildfly.swarm.container.Fraction;
import org.wildfly.swarm.datasources.Datasource;
import org.wildfly.swarm.datasources.DatasourcesFraction;
import org.wildfly.swarm.datasources.Driver;

/**
 * @author Ken Finnigan
 */
public class JPAFraction implements Fraction {

    private String defaultDatasourceName;
    private boolean inhibitDefaultDatasource = false;

    public JPAFraction() {
    }

    public JPAFraction inhibitDefaultDatasource() {
        this.inhibitDefaultDatasource = true;
        return this;
    }

    public JPAFraction defaultDatasourceName(String datasourceName) {
        this.defaultDatasourceName = datasourceName;
        return this;
    }

    public String defaultDatasourceName() {
        return "java:jboss/datasources/" + this.defaultDatasourceName;
    }

    @Override
    public void initialize(Container.InitContext initContext) {
        if (!inhibitDefaultDatasource) {
            initContext.fraction(new DatasourcesFraction()
                    .driver(new Driver("h2")
                            .datasourceClassName("org.h2.Driver")
                            .xaDatasourceClassName("org.h2.jdbcx.JdbcDataSource")
                            .module("com.h2database.h2"))
                    .datasource(new Datasource("ExampleDS")
                            .driver("h2")
                            .connectionURL("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE")
                            .authentication("sa", "sa")));

            this.defaultDatasourceName = "ExampleDS";
        }
    }
}
