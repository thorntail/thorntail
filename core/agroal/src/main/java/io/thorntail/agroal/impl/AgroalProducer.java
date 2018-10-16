package io.thorntail.agroal.impl;

import io.agroal.api.AgroalDataSource;
import io.agroal.api.configuration.AgroalConnectionPoolConfiguration;
import io.agroal.api.configuration.supplier.AgroalDataSourceConfigurationSupplier;
import io.agroal.api.exceptionsorter.MySQLExceptionSorter;
import io.agroal.api.exceptionsorter.OracleExceptionSorter;
import io.agroal.api.exceptionsorter.PostgreSQLExceptionSorter;
import io.agroal.api.security.NamePrincipal;
import io.agroal.api.security.SimplePassword;
import io.agroal.api.transaction.TransactionIntegration;
import io.agroal.narayana.NarayanaTransactionIntegration;
import io.thorntail.agroal.AgroalPoolMetaData;
import io.thorntail.jdbc.DriverMetaData;
import io.thorntail.jdbc.impl.JDBCDriverRegistry;

import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import javax.sql.XADataSource;
import javax.transaction.TransactionManager;
import javax.transaction.TransactionSynchronizationRegistry;
import java.sql.Driver;
import java.sql.SQLException;
import java.time.Duration;
import java.util.logging.Level;
import java.util.logging.Logger;

import static io.agroal.api.configuration.AgroalConnectionPoolConfiguration.ConnectionValidator.emptyValidator;
import static io.agroal.api.configuration.AgroalConnectionPoolConfiguration.ExceptionSorter.emptyExceptionSorter;
import static io.agroal.api.transaction.TransactionIntegration.none;
import static java.lang.Integer.MAX_VALUE;
import static java.time.Duration.ZERO;

@ApplicationScoped
public class AgroalProducer {

    private static final Logger log = Logger.getLogger(AgroalProducer.class.getName());

    private AgroalPoolMetaData metaData;

    private AgroalDataSource agroalDataSource;

    @Inject
    private TransactionManager transactionManager;

    @Inject
    private TransactionSynchronizationRegistry transactionSynchronizationRegistry;

    @Inject
    private InitialContext jndi;

    @Inject
    JDBCDriverRegistry jdbcDriverRegistry;

    public AgroalDataSource deploy(AgroalPoolMetaData metaData) throws SQLException {

        this.metaData = metaData;

        if (jdbcDriverRegistry.get(metaData.getDriver()) == null) {
            AgroalMessages.MESSAGES.noRegisteredJDBCdrivers();
            return null;
        }

        AgroalDataSource dataSource = getDatasource();

        try {
            jndi.bind(metaData.getJNDIName(), dataSource);
        } catch (NamingException e) {
            e.printStackTrace();
        }
        return dataSource;
    }

    @Produces
    @ApplicationScoped
    public AgroalDataSource getDatasource() throws SQLException {
        Class<?> providerClass = null;
        try {
            providerClass = this.getClass().getClassLoader().loadClass(jdbcDriverRegistry.get(this.metaData.getDriver()).getDriverClassName());
        } catch (ClassNotFoundException e) {
            AgroalMessages.MESSAGES.noJDBCdriverClass(jdbcDriverRegistry.get(this.metaData.getDriver()).getDriverClassName());
        }
        if (this.metaData.isXa()) {
            if (!XADataSource.class.isAssignableFrom(providerClass)) {
                throw new RuntimeException("Driver is not an XA datasource and xa has been configured");
            }
        } else {
            if (providerClass != null && !DataSource.class.isAssignableFrom(providerClass) && !Driver.class.isAssignableFrom(providerClass)) {
                throw new RuntimeException("Driver is an XA datasource and xa has been configured");
            }
        }
        AgroalDataSourceConfigurationSupplier dataSourceConfiguration = new AgroalDataSourceConfigurationSupplier();

        //Configure datasource
        dataSourceConfiguration.connectionPoolConfiguration().connectionFactoryConfiguration().jdbcUrl(this.metaData.getConnectionUrl());
        dataSourceConfiguration.connectionPoolConfiguration().connectionFactoryConfiguration().connectionProviderClass(providerClass);

        if (this.metaData.isJta() || this.metaData.isXa()) {
            TransactionIntegration txIntegration = new NarayanaTransactionIntegration(transactionManager, transactionSynchronizationRegistry, null, this.metaData.isConnectable());
            dataSourceConfiguration.connectionPoolConfiguration().transactionIntegration(txIntegration);
        }

        // use the name / password from the callbacks
        if (this.metaData.getUsername() != null) {
            dataSourceConfiguration.connectionPoolConfiguration().connectionFactoryConfiguration().principal(new NamePrincipal(this.metaData.getUsername()));
        }
        if (this.metaData.getPassword() != null) {
            dataSourceConfiguration.connectionPoolConfiguration().connectionFactoryConfiguration().credential(new SimplePassword(this.metaData.getPassword()));
        }

        //Configure pool
        dataSourceConfiguration.connectionPoolConfiguration().acquisitionTimeout(this.metaData.getAcquisitionTimeout());
        dataSourceConfiguration.connectionPoolConfiguration().maxSize(this.metaData.getMaxSize());
        dataSourceConfiguration.connectionPoolConfiguration().maxSize(this.metaData.getMinSize());
        dataSourceConfiguration.connectionPoolConfiguration().initialSize(this.metaData.getInitialSize());
        dataSourceConfiguration.connectionPoolConfiguration().leakTimeout(this.metaData.getLeakTimeout());
        dataSourceConfiguration.connectionPoolConfiguration().reapTimeout(this.metaData.getReapTimeout());
        dataSourceConfiguration.connectionPoolConfiguration().validationTimeout(this.metaData.getValidationTimeout());

        if (this.metaData.validateConnection())
            dataSourceConfiguration.connectionPoolConfiguration().connectionValidator(AgroalConnectionPoolConfiguration.ConnectionValidator.defaultValidator());

        switch (jdbcDriverRegistry.get(this.metaData.getDriver()).getId()){
            case "mysql":
                dataSourceConfiguration.connectionPoolConfiguration().exceptionSorter(new MySQLExceptionSorter());
                break;
            case "oracle":
                dataSourceConfiguration.connectionPoolConfiguration().exceptionSorter(new OracleExceptionSorter());
                break;
            case "postgresql":
                dataSourceConfiguration.connectionPoolConfiguration().exceptionSorter(new PostgreSQLExceptionSorter());
                break;
            case "h2":
            default:
                dataSourceConfiguration.connectionPoolConfiguration().exceptionSorter(AgroalConnectionPoolConfiguration.ExceptionSorter.defaultExceptionSorter());
        }

        agroalDataSource = AgroalDataSource.from(dataSourceConfiguration);
        log.log(Level.INFO, "Started data source " + this.metaData.getConnectionUrl());
        return agroalDataSource;
    }

    @PreDestroy
    public void stop() {
        if (agroalDataSource != null) {
            agroalDataSource.close();
        }
    }

    public static Logger getLog() {
        return log;
    }

    public AgroalDataSource getAgroalDataSource() {
        return agroalDataSource;
    }

}
