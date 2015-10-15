package org.wildfly.swarm.integration.fractions;

import java.util.List;
import java.util.function.Consumer;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.wildfly.swarm.config.datasources.DataSource;
import org.wildfly.swarm.config.datasources.JdbcDriver;
import org.wildfly.swarm.connector.ConnectorFraction;
import org.wildfly.swarm.container.Container;
import org.wildfly.swarm.container.Fraction;
import org.wildfly.swarm.datasources.DatasourcesFraction;
import org.wildfly.swarm.integration.base.AbstractWildFlySwarmTestCase;
import org.wildfly.swarm.jca.JCAFraction;
import org.wildfly.swarm.jpa.JPAFraction;
import org.wildfly.swarm.logging.LoggingFraction;
import org.wildfly.swarm.naming.NamingFraction;
import org.wildfly.swarm.transactions.TransactionsFraction;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author Ken Finnigan
 */
public class FractionHandlingTest extends AbstractWildFlySwarmTestCase {

    @Rule
    public TestName name = new TestName();

    Container container = null;

    @Before
    public void setup() throws Exception {
        System.out.println("Starting test: " + name.getMethodName());
        container = newContainer();
    }

    @After
    public void shutdown() throws Exception {
        try {
            container.stop();
        } catch (NullPointerException npe) {
            // Ignore as it's likely caused by an error in starting the container, which is reported separately
        }

        System.out.println();
    }

//    @Test
    public void allDefaultFractionsPresent() throws Exception {
        container.start();

        verifyFractions(container.fractions(), dsFraction -> verifyValidDataSourceFraction(dsFraction, "ExampleDS", "h2"));
    }

//    @Test
    public void userSpecifiedJPAFractionOverridesDefault() throws Exception {
        container.fraction(new MyJPAFraction());

        container.start();

        verifyFractions(container.fractions(), this::verifyEmptyDataSourceFraction);
    }

//    @Test
    public void userSpecifiedFractionOverridesDependentFraction() throws Exception {
        container.fraction(new DatasourcesFraction()
                        .jdbcDriver(new JdbcDriver("myDriver")
                                .driverDatasourceClassName("org.h2.Driver")
                                .driverXaDatasourceClassName("org.h2.jdbcx.JdbcDataSource")
                                .driverModuleName("com.h2database.h2"))
                        .dataSource(new DataSource("MyDS")
                                .driverName("myDriver")
                                .connectionUrl("jdbc:myDriver:mem:test;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE")
                                .userName("sa")
                                .password("sa"))
        );

        container.start();

        verifyFractions(container.fractions(), dsFraction -> verifyValidDataSourceFraction(dsFraction, "MyDS", "myDriver"));
    }

    private void verifyEmptyDataSourceFraction(DatasourcesFraction dsFraction) {
        assertThat(dsFraction).overridingErrorMessage("DataSourceFraction was null").isNotNull();
        assertThat(dsFraction.subresources().dataSources()).overridingErrorMessage("DataSources were specified").isEmpty();
        assertThat(dsFraction.subresources().jdbcDrivers()).overridingErrorMessage("Drivers were specified").isEmpty();
    }

    private void verifyValidDataSourceFraction(DatasourcesFraction dsFraction, String dsName, String driverName) {
        // Verify default DataSource Fraction
        assertThat(dsFraction).overridingErrorMessage("DataSourceFraction was null").isNotNull();
        assertThat(dsFraction.subresources().dataSources()).overridingErrorMessage("No DataSources specified").isNotEmpty();
        assertThat(dsFraction.subresources().dataSources().size()).overridingErrorMessage("More than one Datasource specified").isEqualTo(1);
        assertThat(dsFraction.subresources().jdbcDrivers()).overridingErrorMessage("No drivers specified").isNotEmpty();
        assertThat(dsFraction.subresources().jdbcDrivers().size()).overridingErrorMessage("More than one Driver specified").isEqualTo(1);

        // Verify DataSource
        DataSource ds = dsFraction.subresources().dataSources().get(0);
        assertThat(ds.getKey()).overridingErrorMessage("DataSource name is not " + dsName).isEqualTo(dsName);
        assertThat(ds.driverName()).overridingErrorMessage("DataSource driver is not " + driverName).isEqualTo(driverName);
        assertThat(ds.connectionUrl()).isEqualTo("jdbc:" + driverName + ":mem:test;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE");
        assertThat(ds.userName()).isEqualTo("sa");
        assertThat(ds.password()).isEqualTo("sa");

        // Verify Driver
        JdbcDriver driver = dsFraction.subresources().jdbcDrivers().get(0);
        assertThat(driver.getKey()).overridingErrorMessage("Driver name is not " + driverName).isEqualTo(driverName);
        assertThat(driver.driverDatasourceClassName()).overridingErrorMessage("Driver datasource class name is not 'org.h2.Driver'").isEqualTo("org.h2.Driver");
        assertThat(driver.driverXaDatasourceClassName()).overridingErrorMessage("Driver XA datasource class name is not 'org.h2.jdbcx.JdbcDataSource'").isEqualTo("org.h2.jdbcx.JdbcDataSource");
    }

    private void verifyFractions(List<Fraction> installedFractions, Consumer<DatasourcesFraction> dsFractionVerifier) throws Exception {
        boolean logging, jpa, datasources, connector, jca, trans, naming, undertow, jaxrs;
        logging = jpa = datasources = connector = jca = trans = naming = undertow = jaxrs = false;
        DatasourcesFraction dsFraction = null;

        for (Fraction each : installedFractions) {
            if (LoggingFraction.class.isAssignableFrom(each.getClass())) {
                logging = true;
            } else if (JPAFraction.class.isAssignableFrom(each.getClass())) {
                jpa = true;
            } else if (DatasourcesFraction.class.isAssignableFrom(each.getClass())) {
                datasources = true;
                dsFraction = (DatasourcesFraction) each;
            } else if (ConnectorFraction.class.isAssignableFrom(each.getClass())) {
                connector = true;
            } else if (JCAFraction.class.isAssignableFrom(each.getClass())) {
                jca = true;
            } else if (TransactionsFraction.class.isAssignableFrom(each.getClass())) {
                trans = true;
            } else if (NamingFraction.class.isAssignableFrom(each.getClass())) {
                naming = true;
            } else if (each.getClass().getName().equals("org.wildfly.swarm.undertow.UndertowFraction")) {
                undertow = true;
            } else if (each.getClass().getName().equals("org.wildfly.swarm.jaxrs.JAXRSFraction")) {
                jaxrs = true;
            }
        }

        // Verify correct fractions were installed
        assertThat(logging).overridingErrorMessage("Logging Fraction was not installed").isTrue();
        assertThat(jpa).overridingErrorMessage("JPA Fraction was not installed").isTrue();
        assertThat(datasources).overridingErrorMessage("Datasources Fraction was not installed").isTrue();
        assertThat(connector).overridingErrorMessage("Connector Fraction was not installed").isTrue();
        assertThat(jca).overridingErrorMessage("JCA Fraction was not installed").isTrue();
        assertThat(trans).overridingErrorMessage("Transaction Fraction was not installed").isTrue();
        assertThat(naming).overridingErrorMessage("Naming Fraction was not installed").isTrue();

        assertThat(undertow).overridingErrorMessage("Undertow Fraction should not be installed").isFalse();
        assertThat(jaxrs).overridingErrorMessage("JAX-RS Fraction should not be installed").isFalse();

        dsFractionVerifier.accept(dsFraction);
    }
}
