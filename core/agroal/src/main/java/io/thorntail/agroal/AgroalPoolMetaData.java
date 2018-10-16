package io.thorntail.agroal;

import io.thorntail.jdbc.DriverMetaData;
import io.thorntail.TraceMode;

import java.time.Duration;

import static java.time.Duration.ZERO;

/**
 * Meta-data describing an agroal connection pool.
 */
public class AgroalPoolMetaData {

    /**
     * Construct
     *
     * @param id The unique datasource identifier.
     */
    public AgroalPoolMetaData(String id) {
        this.id = id;
    }

    /**
     * Retrieve the unique identifier.
     *
     * @return The unique identifier.
     */
    public String getId() {
        return this.id;
    }

    /**
     * Set the JDBC driver identifier.
     *
     * @param driver The driver identifier.
     * @return This meta-data object.
     * @see DriverMetaData#getId()
     */
    public AgroalPoolMetaData setDriver(String driver) {
        this.driver = driver;
        return this;
    }

    /**
     * Retrieve the JDBC driver identifier.
     *
     * @return The JDBC driver identifier.
     * @see DriverMetaData#getId()
     */
    public String getDriver() {
        return this.driver;
    }

    /**
     * Set the connection username, if required.
     *
     * @param username The connection username.
     * @return This meta-data object.
     */
    public AgroalPoolMetaData setUsername(String username) {
        this.username = username;
        return this;
    }

    /**
     * Retrieve the connection username.
     *
     * @return The connection username, or {@code null} if unset.
     */
    public String getUsername() {
        return this.username;
    }

    /**
     * Set the connection, password if required.
     *
     * @param password The connection password.
     * @return This meta-data object.
     */
    public AgroalPoolMetaData setPassword(String password) {
        this.password = password;
        return this;
    }

    /**
     * Retrieve the connection password.
     *
     * @return The connectoin password, or {@code null} if unset.
     */
    public String getPassword() {
        return this.password;
    }

    /**
     * Set the connection URL.
     *
     * @param connectionUrl The connection URL.
     * @return This meta-data object.
     */
    public AgroalPoolMetaData setConnectionUrl(String connectionUrl) {
        this.connectionUrl = connectionUrl;
        return this;
    }

    /**
     * Retrieve the connection URL.
     *
     * @return The connection URL.
     */
    public String getConnectionUrl() {
        return this.connectionUrl;
    }

    /**
     * Set the explicit JNDI name to which to bind this datasource.
     *
     * <p>If unset, a name will be calculated based upon {@link #getId()}.</p>
     *
     * @param jndiName The JNDI name to which to bind this datasource.
     * @return
     */
    public AgroalPoolMetaData setJNDIName(String jndiName) {
        this.jndiName = jndiName;
        return this;
    }

    /**
     * Retrieve the explicit JNDI name to which to bind this datasource.
     *
     * <p>If unset, a name will be calculated based upon {@link #getId()}.</p>
     *
     * @return The JNDI name to which to bind this datasource, or {@code null} if unset.
     */
    public String getJNDIName() {
        return this.jndiName;
    }

    /**
     * Enable tracing for this datasource.
     *
     * <p>Requires the {@code opentracing} component to be available if set to {@code true}.</p>
     *
     * @param trace Flag to determine if this datasource should be traced.
     */
    public void setTraceMode(TraceMode trace) {
        this.trace = trace;
    }

    /**
     * Determine if tracing is enabled for this datasource.
     *
     * <p>Requires the {@code opentracing} component to be available if set to {@code true}.</p>
     *
     * @return {@code true} if tracing is requested, otherwise {@code false}.
     */
    public TraceMode getTraceMode() {
        return this.trace;
    }

    /**
     * Retrieve the maximum connection pool szie
     *
     * @return The maximum database connection pool size.
     */
    public int getMaxSize() {
        return maxSize;
    }

    /**
     * Set the maximum size of the database connection pool.
     *
     * @param maxSize The max pool size.
     * @return This meta-data object.
     */
    public AgroalPoolMetaData setMaxSize(int maxSize) {
        this.maxSize = maxSize;
        return this;
    }

    /**
     * Retrieve the minimum connection pool szie
     *
     * @return The minimum database connection pool size.
     */
    public int getMinSize() {
        return minSize;
    }

    /**
     * Set the maximum size of the database connection pool.
     *
     * @param minSize The max pool size.
     * @return This meta-data object.
     */
    public AgroalPoolMetaData setMinSize(int minSize) {
        this.minSize = minSize;
        return this;
    }

    /**
     * Retrieve the initial connection  pool size
     *
     * @return The initial database connection pool size.
     */
    public int getInitialSize() {
        return initialSize;
    }

    /**
     * Set the initial connection pool size
     *
     * @param initialSize The initial pool size.
     * @return This meta-data object.
     */
    public AgroalPoolMetaData setInitialSize(int initialSize) {
        this.initialSize = initialSize;
        return this;
    }

    /**
     * Retrieve the connection leak timeout {@code Duration} for the connection pool.
     *
     * @return Leak timeout {@code Duration}
     */
    public Duration getLeakTimeout() {
        return leakTimeout;
    }

    /**
     * Set the connection pool leak timeout
     *
     * @param leakTimeout The initial pool size.
     * @return This meta-data object.
     */
    public AgroalPoolMetaData setLeakTimeout(Duration leakTimeout) {
        this.leakTimeout = leakTimeout;
        return this;
    }

    /**
     * Retrieve the connection validation timeout {@code Duration} for the connection pool.
     *
     * @return Validation timeout {@code Duration}
     */
    public Duration getValidationTimeout() {
        return validationTimeout;
    }


    /**
     * Set the connection pool validation timeout {@code Duration} for the connection pool.
     *
     * @param validationTimeout The initial pool size.
     * @return This meta-data object.
     */
    public AgroalPoolMetaData setValidationTimeout(Duration validationTimeout) {
        this.validationTimeout = validationTimeout;
        return this;
    }

    /**
     * Retrieve the connection reap timeout {@code Duration} for the connection pool.
     *
     * @return Reap timeout {@code Duration}
     */
    public Duration getReapTimeout() {
        return reapTimeout;
    }

    /**
     * Set the connection pool reap timeout {@code Duration} for the connection pool.
     *
     * @param reapTimeout The initial pool size.
     * @return This meta-data object.
     */
    public AgroalPoolMetaData setReapTimeout(Duration reapTimeout) {
        this.reapTimeout = reapTimeout;
        return this;
    }

    /**
     * Retrieve the connection acquisition timeout {@code Duration} for the connection pool.
     *
     * @return acquisition timeout {@code Duration}
     */
    public Duration getAcquisitionTimeout() {
        return acquisitionTimeout;
    }

    /**
     * Set the connection pool acquisition timeout {@code Duration} for the connection pool.
     *
     * @param acquisitionTimeout The initial pool size.
     * @return This meta-data object.
     */
    public AgroalPoolMetaData setAcquisitionTimeout(Duration acquisitionTimeout) {
        this.acquisitionTimeout = acquisitionTimeout;
        return this;
    }

    /**
     * Is connections validation enabled.
     *
     * @return true if connection validation is enabled, otherwise false.
     */
    public boolean validateConnection() {
        return validateConnection;
    }

    /**
     * Set whether connections should be validated.
     *
     * @param validateConnection The initial pool size.
     * @return This meta-data object.
     */
    public AgroalPoolMetaData setValidateConnection(boolean validateConnection) {
        this.validateConnection = validateConnection;
        return this;
    }

    /**
     * Are transactions are managed by JTA.
     *
     * @return true if transactions are managed by a Transaction Manager, otherwise false.
     */
    public boolean isJta() {
        return jta;
    }

    /**
     * Set whether transactions are managed by JTA.
     *
     * @param jta are transactions managed by a TransactionManager.
     * @return This meta-data object.
     */
    public AgroalPoolMetaData setJta(boolean jta) {
        this.jta = jta;
        return this;
    }

    /**
     * Are database connections XA compliant.
     *
     * @return true if database connections are XA compliant, otherwise false.
     */
    public boolean isXa() {
        return xa;
    }

    /**
     * Set whether database connections are XA compliant.
     *
     * @param xa are database connections are XA compliant.
     * @return This meta-data object.
     */
    public AgroalPoolMetaData setXa(boolean xa) {
        this.xa = xa;
        return this;

    }
    public boolean isConnectable() {
        return connectable;
    }

    public AgroalPoolMetaData setConnectable(boolean connectable) {
        this.connectable = connectable;
        return this;
    }


    @Override
    public String toString() {
        return "[DS: id=" + this.id + "; driver=" + this.driver + "; jndiName=" + this.jndiName + "; trace=" + this.trace + "]";
    }

    //DataSource configuration
    private final String id;

    private String driver;

    private String username;

    private String password;

    private String jndiName;

    private String connectionUrl;

    private boolean jta = true;

    private boolean connectable;

    private boolean xa = false;


    //ConnectionPool configuration
    private int initialSize;

    private volatile int minSize = 0;

    private int maxSize;

    private Duration leakTimeout = ZERO;

    private Duration validationTimeout = ZERO;

    private Duration reapTimeout = ZERO;

    private volatile Duration acquisitionTimeout = ZERO;

    private boolean validateConnection = false;

    private TraceMode trace = TraceMode.OFF;

}
