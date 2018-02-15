package org.jboss.unimbus.jdbc;

public class DriverMetaData {

    public DriverMetaData(String id) {
        this.id = id;
    }

    public String getId() {
        return this.id;
    }

    public DriverMetaData setDriverClassName(String driverClassName) {
        this.driverClassName = driverClassName;
        return this;
    }

    public String getDriverClassName() {
        return this.driverClassName;
    }

    public DriverMetaData setDataSourceClassName(String dataSourceClassName) {
        this.dataSourceClassName = dataSourceClassName;
        return this;
    }

    public String getDataSourceClassName() {
        return this.dataSourceClassName;
    }

    public String toString() {
        return this.driverClassName;
    }

    private final String id;

    private String driverClassName;

    private String dataSourceClassName;
}
