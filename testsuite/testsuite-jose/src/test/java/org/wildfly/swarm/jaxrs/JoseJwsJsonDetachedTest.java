package org.wildfly.swarm.jaxrs;

import java.io.StringReader;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.swarm.jose.Jose;
import org.wildfly.swarm.jose.JoseLookup;

@RunWith(Arquillian.class)
public class JoseJwsJsonDetachedTest {
    private Client client;
    
    @Before
    public void setup() throws Exception {
        client = ClientBuilder.newClient();
    }
    
    @After
    public void tearDown() throws Exception {
        client.close();
    }
    
    @Deployment
    public static Archive<?> createDeployment() throws Exception {
        JAXRSArchive deployment = ShrinkWrap.create(JAXRSArchive.class);
        deployment.addResource(MyResource.class);
        deployment.addResource(SecuredApplication.class);
        deployment.addResource(JoseExceptionMapper.class); 
        deployment.addAllDependencies();
        deployment.addAsResource("keystore.jks");
        deployment.addAsResource("project-jws-json-detached.yml", "project-defaults.yml");
        return deployment;
    }
    
    @Test
    public void testJwsCompactDetached() throws Exception {
        Jose jose = JoseLookup.lookup().get();
        Response r = client.target("http://localhost:8080/signDetached")
                                .request(MediaType.TEXT_PLAIN)
                                .header("DetachedData", "Hello")
                                .post(Entity.entity(jose.sign("Hello"), MediaType.TEXT_PLAIN));
        String signedData = r.readEntity(String.class);
        Assert.assertEquals("Hello", jose.verifyDetached(signedData, r.getHeaderString("DetachedData")));
        // Now confirm it is actually JWS JSON
        JsonReader jsonReader = Json.createReader(new StringReader(signedData));
        JsonObject jwsJson = jsonReader.readObject();
        // Flattened JWS JSON with the encoded payload (single recipient only)
        Assert.assertEquals(2, jwsJson.size());
        Assert.assertNotNull(jwsJson.get("protected"));
        Assert.assertNotNull(jwsJson.get("signature"));
    }
}
