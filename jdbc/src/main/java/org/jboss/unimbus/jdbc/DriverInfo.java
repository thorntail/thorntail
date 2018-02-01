package org.jboss.unimbus.jdbc;

import javax.enterprise.inject.Vetoed;

public class DriverInfo {

    public DriverInfo(String id) {
        this.id = id;
    }

    public String getId() {
        return this.id;
    }

    public DriverInfo setDriverClassName(String driverClassName) {
        this.driverClassName = driverClassName;
        return this;
    }

    public String getDriverClassName() {
        return this.driverClassName;
    }

    public DriverInfo setDataSourceClassName(String dataSourceClassName) {
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
