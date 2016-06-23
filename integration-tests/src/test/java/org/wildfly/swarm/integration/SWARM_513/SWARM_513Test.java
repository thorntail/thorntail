package org.wildfly.swarm.integration.SWARM_513;

import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.RequestBody;
import org.junit.Assert;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.ClassLoaderAsset;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.wildfly.swarm.ContainerFactory;
import org.wildfly.swarm.container.Container;
import org.wildfly.swarm.datasources.DatasourcesFraction;
import org.wildfly.swarm.integration.base.AbstractWildFlySwarmTestCase;
import org.wildfly.swarm.jaxrs.JAXRSArchive;
import org.wildfly.swarm.jaxrs.JAXRSFraction;
import org.wildfly.swarm.jpa.JPAFraction;
import org.wildfly.swarm.logging.LoggingFraction;
import org.wildfly.swarm.transactions.TransactionsFraction;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author Heiko Braun
 */
public class SWARM_513Test extends AbstractWildFlySwarmTestCase implements ContainerFactory {

    @Rule
    public TestName name = new TestName();

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

    @Deployment
    public static Archive createDeployment() throws Exception {
        JAXRSArchive deployment = ShrinkWrap.create(JAXRSArchive.class);
        deployment.addResource(TicketEndpoint.class);
        deployment.addResource(Ticket.class);
        deployment.addResource(TicketDTO.class);

        deployment.addAsWebInfResource(new ClassLoaderAsset("META-INF/persistence.xml", SWARM_513Test.class.getClassLoader()), "classes/META-INF/persistence.xml");
        deployment.addAsWebInfResource(new ClassLoaderAsset("META-INF/import.sql", SWARM_513Test.class.getClassLoader()), "classes/META-INF/import.sql");

        deployment.addAllDependencies();

        return deployment;
    }

    @Override
    public Container newContainer(String... args) throws Exception {
        return new Container()
                .fraction(TransactionsFraction.createDefaultFraction())
                .fraction(new JPAFraction().inhibitDefaultDatasource())
                .fraction(new DatasourcesFraction())
                .fraction(LoggingFraction.createDebugLoggingFraction())
                .fraction(new JAXRSFraction());
    }

    @Test
    @RunAsClient
    public void testEndpointAvailability() throws Exception {

        container.start();
        container.deploy(createDeployment());

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(DEFAULT_URL + "tickets")
                .build();

        Response response = client.newCall(request).execute();
        Assert.assertEquals(200, response.code());
        assertThat(response.body().string()).contains("<collection/>");
    }

    @Test
    @RunAsClient
    public void testCreateTicket() throws Exception {

        container.start();
        container.deploy(createDeployment());

        StringBuffer sb = new StringBuffer();
        sb.append("<ticketDTO>");
        sb.append("<id>1234</id><price>12.00</price>");
        sb.append("</ticketDTO>");

        OkHttpClient client = new OkHttpClient();

        RequestBody body = RequestBody.create(MediaType.parse("text/xml; charset=utf-8"), sb.toString());
        Request request = new Request.Builder()
                .url(DEFAULT_URL + "tickets")
                .post(body)
                .build();

        Response response = client.newCall(request).execute();
        Assert.assertEquals(201,response.code());
    }

    private Container container;
}
