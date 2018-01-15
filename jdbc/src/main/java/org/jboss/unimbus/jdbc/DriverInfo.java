package org.jboss.unimbus.jdbc;

import javax.enterprise.inject.Vetoed;

public class DriverInfo {

    public DriverInfo() {
        this(null);
    }

    public DriverInfo(String driverClassName) {
        this.driverClassName = driverClassName;
    }

    public String getDriverClassName() {
        return this.driverClassName;
    }

    private final String driverClassName;
}
