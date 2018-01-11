package org.jboss.unimbus.jdbc;

public class DriverInfo {

    public DriverInfo(String driverClassName) {
        this.driverClassName = driverClassName;
    }

    public String getDriverClassName() {
        return this.driverClassName;
    }

    private final String driverClassName;
}
