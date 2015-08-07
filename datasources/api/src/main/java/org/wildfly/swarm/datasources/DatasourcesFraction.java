package org.wildfly.swarm.datasources;

import java.util.ArrayList;
import java.util.List;

import org.wildfly.swarm.container.Fraction;

/**
 * @author Bob McWhirter
 */
public class DatasourcesFraction implements Fraction {

    private List<Datasource> datasources = new ArrayList<>();

    private List<Driver> drivers = new ArrayList<>();

    public DatasourcesFraction() {
    }

    public DatasourcesFraction datasource(Datasource datasource) {
        this.datasources.add(datasource);
        return this;
    }

    public List<Datasource> datasources() {
        return this.datasources;
    }

    public DatasourcesFraction driver(Driver driver) {
        this.drivers.add(driver);
        return this;
    }

    public List<Driver> drivers() {
        return this.drivers;
    }

}
