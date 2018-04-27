package io.thorntail.jdbc;

/**
 * JDBC driver meta-data.
 *
 * <p>Instances of this class are usually auto-created based upon the availability of JDBC drivers
 * on the classpath.</p>
 *
 * <p>In the event that an application requires a non-detected JDBC driver, it may {@code @Produce} an
 * {@code @ApplicationScoped} instance with the appropriate values.</p>
 *
 * <p>Each instance of {@code DriverMetaData} will register a JDBC driver which may be referenced
 * by a datasource.</p>
 *
 * @author Ken Finnigan
 * @author Bob McWhirter
 */
public class DriverMetaData {

    /**
     * Construct a new instance.
     *
     * @param id The unique identifier for the JDBC driver.
     */
    public DriverMetaData(String id) {
        this.id = id;
    }

    /**
     * Retrieve the unique identifier for the JDBC driver.
     *
     * @return
     */
    public String getId() {
        return this.id;
    }

    /**
     * Set the driver class name.
     *
     * @param driverClassName The driver class name.
     * @return This meta-data object.
     */
    public DriverMetaData setDriverClassName(String driverClassName) {
        this.driverClassName = driverClassName;
        return this;
    }

    /**
     * Retrieve the driver class name.
     *
     * @return The driver class name.
     */
    public String getDriverClassName() {
        return this.driverClassName;
    }

    /**
     * Set the datasource class name.
     *
     * @param dataSourceClassName The datasource class name.
     * @return This meta-data object.
     */
    public DriverMetaData setDataSourceClassName(String dataSourceClassName) {
        this.dataSourceClassName = dataSourceClassName;
        return this;
    }

    /**
     * Retrieve the datasource class name.
     *
     * @return The datasource class name.
     */
    public String getDataSourceClassName() {
        return this.dataSourceClassName;
    }

    private final String id;

    private String driverClassName;

    private String dataSourceClassName;
}
