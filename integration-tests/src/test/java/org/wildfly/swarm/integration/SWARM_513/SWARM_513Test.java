package org.wildfly.swarm.integration.SWARM_513;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import junit.framework.Assert;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.ClassLoaderAsset;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.swarm.ContainerFactory;
import org.wildfly.swarm.container.Container;
import org.wildfly.swarm.datasources.DatasourcesFraction;
import org.wildfly.swarm.integration.base.TestConstants;
import org.wildfly.swarm.jaxrs.JAXRSArchive;
import org.wildfly.swarm.jaxrs.JAXRSFraction;
import org.wildfly.swarm.jpa.JPAFraction;
import org.wildfly.swarm.transactions.TransactionsFraction;

/**
 * @author Heiko Braun
 */
@RunWith(Arquillian.class)
public class SWARM_513Test implements ContainerFactory {

    @Deployment(testable = true)
    public static Archive createDeployment() throws Exception {
        JAXRSArchive deployment = ShrinkWrap.create(JAXRSArchive.class, "swarm_513.war");
        deployment.addResource(TicketEndpoint.class);
        deployment.addResource(Ticket.class);
        deployment.addResource(Tickets.class);
        deployment.addResource(TicketDTO.class);

        deployment.addAsWebInfResource(new ClassLoaderAsset("META-INF/persistence.xml", SWARM_513Test.class.getClassLoader()), "classes/META-INF/persistence.xml");
        deployment.addAsWebInfResource(new ClassLoaderAsset("META-INF/import.sql", SWARM_513Test.class.getClassLoader()), "classes/META-INF/import.sql");

        return deployment;
    }

    @Override
    public Container newContainer(String... args) throws Exception {
        return new Container()
                .fraction(TransactionsFraction.createDefaultFraction())
                .fraction(new JPAFraction().inhibitDefaultDatasource())
                .fraction(new DatasourcesFraction()
                                  .jdbcDriver("h2", (d) -> {
                                      d.driverClassName("org.h2.Driver");
                                      d.xaDatasourceClass("org.h2.jdbcx.JdbcDataSource");
                                      d.driverModuleName("com.h2database.h2");
                                  })
                                  .dataSource("ExampleDS", (ds) -> {
                                      ds.driverName("h2");
                                      ds.connectionUrl("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE");
                                      ds.userName("sa");
                                      ds.password("sa");
                                  }))
                .fraction(new JAXRSFraction());
    }

    @Test
    @InSequence(2)
    public void testEndpointAvailability() throws Exception {

        checkAvailability();
    }

    @Test
    @RunAsClient
    @InSequence(3)
    public void testEndpointAvailabilityClient() throws Exception {
        checkAvailability();
    }

    private void checkAvailability() throws Exception {
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target(TestConstants.DEFAULT_URL).path("tickets");

        Response response = target.request(MediaType.TEXT_XML).get();
        Assert.assertEquals(200, response.getStatus());
        Tickets dto = response.readEntity(Tickets.class);
        Assert.assertNotNull("Response is not a list", dto.getTickets());
        Assert.assertEquals("Expected a single entity in response collection", dto.getTickets().size(), 1);
        Assert.assertEquals("Ticket ID didn't match expected output", dto.getTickets().get(0).getId(), new Long(1));
    }

    @Test
    @RunAsClient
    @InSequence(1)
    public void testCreateTicketClient() throws Exception {

        StringBuffer sb = new StringBuffer();
        sb.append("<ticketDTO>");
        sb.append("<price>12.00</price>");
        sb.append("</ticketDTO>");

        Client client = ClientBuilder.newClient();
        WebTarget target = client.target(TestConstants.DEFAULT_URL).path("tickets");

        javax.ws.rs.core.Response response = target.request(MediaType.TEXT_XML)
                .post(Entity.entity(sb.toString(), MediaType.TEXT_XML));

        Assert.assertEquals(201,response.getStatus());
        Assert.assertTrue(response.getHeaders().keySet().contains("Location"));
    }

    @Test
    @RunAsClient
    @InSequence(4)
    public void testJSONProvider() throws Exception {

        Client client = ClientBuilder.newClient();
        WebTarget target = client.target(TestConstants.DEFAULT_URL).path("tickets");

        Response response = target.request(MediaType.APPLICATION_JSON).get();
        Assert.assertEquals(200, response.getStatus());

        Assert.assertEquals("[{\"id\":1,\"price\":12.0}]",response.readEntity(String.class));

    }
}
