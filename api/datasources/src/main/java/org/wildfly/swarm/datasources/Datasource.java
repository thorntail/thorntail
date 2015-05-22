package org.wildfly.swarm.datasources;

/**
 * @author Bob McWhirter
 */
public class Datasource {

    private final String name;
    private String connectionUrl;
    private String driverName;

    private String userName;
    private String password;

    public Datasource(String name) {
        this.name = name;
    }

    public String name() {
        return this.name;
    }

    public Datasource connectionURL(String connectionUrl) {
        this.connectionUrl = connectionUrl;
        return this;
    }

    public String connectionURL() {
        return this.connectionUrl;
    }

    public Datasource driver(String driverName) {
        this.driverName = driverName;
        return this;
    }

    public String driver() {
        return this.driverName;
    }

    public Datasource authentication(String userName, String password) {
        this.userName = userName;
        this.password = password;
        return this;
    }

    public String userName() {
        return this.userName;
    }

    public String password() {
        return this.password;
    }

    public String jndiName() {
        return "java:jboss/datasources/" + this.name;
    }

}
