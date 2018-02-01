package org.jboss.unimbus.datasources;

/**
 * Created by bob on 2/1/18.
 */
public class DataSourceMetaData {

    public DataSourceMetaData(String id) {
        this.id = id;
    }

    public String getId() {
        return this.id;
    }

    public DataSourceMetaData setDriver(String driver) {
        this.driver = driver;
        return this;
    }

    public String getDriver() {
        return this.driver;
    }

    public DataSourceMetaData setUsername(String username) {
        this.username = username;
        return this;
    }

    public String getUsername() {
        return this.username;
    }

    public DataSourceMetaData setPassword(String password) {
        this.password = password;
        return this;
    }

    public String getPassword() {
        return this.password;
    }

    public DataSourceMetaData setConnectionUrl(String connectionUrl) {
        this.connectionUrl = connectionUrl;
        return this;
    }

    public String getConnectionUrl() {
        return this.connectionUrl;
    }

    public DataSourceMetaData setJNDIName(String jndiName) {
        this.jndiName = jndiName;
        return this;
    }

    public String getJNDIName() {
        return this.jndiName;
    }

    private final String id;

    private String driver;

    private String username;

    private String password;

    private String jndiName;

    private String connectionUrl;
}
