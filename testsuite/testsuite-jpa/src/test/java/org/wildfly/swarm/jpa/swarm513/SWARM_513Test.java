package org.wildfly.swarm.jpa.swarm513;

import java.io.File;
import java.net.URL;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.ClassLoaderAsset;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.swarm.jaxrs.JAXRSArchive;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author Heiko Braun
 */
@RunWith(Arquillian.class)
public class SWARM_513Test {

    @Deployment(testable = true)
    public static Archive createDeployment() throws Exception {
        URL url = Thread.currentThread().getContextClassLoader().getResource("project-test-defaults-path.yml");
        assertThat(url).isNotNull();
        File projectDefaults = new File(url.toURI());
        JAXRSArchive deployment = ShrinkWrap.create(JAXRSArchive.class, "myapp.war");
        deployment.addResource(TicketEndpoint.class);
        deployment.addClass(Ticket.class);
        deployment.addClass(Tickets.class);
        deployment.addClass(TicketDTO.class);

        deployment.addAsWebInfResource(new ClassLoaderAsset("META-INF/persistence.xml", SWARM_513Test.class.getClassLoader()), "classes/META-INF/persistence.xml");
        deployment.addAsWebInfResource(new ClassLoaderAsset("META-INF/import.sql", SWARM_513Test.class.getClassLoader()), "classes/META-INF/import.sql");

        deployment.addAsResource(projectDefaults, "/project-defaults.yml");
        return deployment;
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
        WebTarget target = client.target("http://localhost:8080").path("tickets");

        javax.ws.rs.core.Response response = target.request(MediaType.TEXT_XML)
                .post(Entity.entity(sb.toString(), MediaType.TEXT_XML));

        Assert.assertEquals(201, response.getStatus());
        Assert.assertTrue(response.getHeaders().keySet().contains("Location"));
    }

    @Test
    @InSequence(2)
    public void testEndpointAvailability() throws Exception {
        checkAvailability();
    }

    //TODO Fix!
//    @Test
//    @RunAsClient
//    @InSequence(3)
    public void testEndpointAvailabilityClient() throws Exception {
        checkAvailability();
    }

    private void checkAvailability() throws Exception {
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target("http://localhost:8080").path("tickets");

        Response response = target.request(MediaType.TEXT_XML).get();
        Assert.assertEquals(200, response.getStatus());
        Tickets dto = response.readEntity(Tickets.class);
        Assert.assertNotNull("Response is not a list", dto.getTickets());
        Assert.assertEquals("Expected a single entity in response collection", dto.getTickets().size(), 1);
        Assert.assertEquals("Ticket ID didn't match expected output", dto.getTickets().get(0).getId(), new Long(1));
    }

    @Test
    @RunAsClient
    @InSequence(4)
    public void testJSONProvider() throws Exception {
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target("http://localhost:8080").path("tickets");

        Response response = target.request(MediaType.APPLICATION_JSON).get();
        Assert.assertEquals(200, response.getStatus());

        Assert.assertEquals("[{\"id\":1,\"price\":12.0}]", response.readEntity(String.class));
    }
}
