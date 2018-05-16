package org.wildfly.swarm.jaxrs;

import java.io.StringReader;
import java.util.Base64;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;

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
public class JoseJsonTest {

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
        deployment.addAsResource("project-jose-json.yml", "project-defaults.yml");
        return deployment;
    }
    
    @Test
    public void testJwsJson() throws Exception {
        Jose jose = JoseLookup.lookup().get();
        String signedData = client.target("http://localhost:8080/sign")
                                .request(MediaType.TEXT_PLAIN)
                                .post(Entity.entity(jose.sign("Hello"), MediaType.TEXT_PLAIN),
                                      String.class);
        Assert.assertEquals("Hello", jose.verify(signedData));
        
        // Now confirm it is actually JWS JSON
        JsonReader jsonReader = Json.createReader(new StringReader(signedData));
        JsonObject jwsJson = jsonReader.readObject();
        // Flattened JWS JSON (single recipient only)
        Assert.assertEquals(3, jwsJson.size());
        Assert.assertNotNull(jwsJson.get("protected"));
        Assert.assertNotNull(jwsJson.get("signature"));
        String encodedPayload = jwsJson.getString("payload");
        Assert.assertEquals("Hello", new String(Base64.getUrlDecoder().decode(encodedPayload)));
    }
    
    @Test
    public void testJweJson() throws Exception {
        Jose jose = JoseLookup.lookup().get();
        String encryptedData = client.target("http://localhost:8080/encrypt")
                                .request(MediaType.TEXT_PLAIN)
                                .post(Entity.entity(jose.encrypt("Hello"), MediaType.TEXT_PLAIN),
                                      String.class);
        Assert.assertEquals("Hello", jose.decrypt(encryptedData));
        
        // Now confirm it is actually JWE JSON
        JsonReader jsonReader = Json.createReader(new StringReader(encryptedData));
        JsonObject jweJson = jsonReader.readObject();
        // Flattened JWE JSON (single recipient only)
        Assert.assertEquals(5, jweJson.size());
        Assert.assertNotNull(jweJson.get("protected"));
        Assert.assertNotNull(jweJson.get("encrypted_key"));
        Assert.assertNotNull(jweJson.get("iv"));
        Assert.assertNotNull(jweJson.get("ciphertext"));
        Assert.assertNotNull(jweJson.get("tag"));
        
    }
    
}
