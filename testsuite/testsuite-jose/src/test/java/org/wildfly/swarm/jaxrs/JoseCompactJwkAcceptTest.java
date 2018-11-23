package org.wildfly.swarm.jaxrs;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.swarm.jose.DecryptionOutput;
import org.wildfly.swarm.jose.Jose;
import org.wildfly.swarm.jose.JoseLookup;
import org.wildfly.swarm.jose.VerificationOutput;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;

@RunWith(Arquillian.class)
public class JoseCompactJwkAcceptTest {

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
        deployment.addAsResource("jwk.keys");
        deployment.addAsResource("project-jwk-store-accept.yml", "project-defaults.yml");
        return deployment;
    }
    
    @Test
    public void testJwsHmacCompact() throws Exception {
        Jose jose = JoseLookup.lookup().get();
        String signedData = client.target("http://localhost:8080/sign")
                                .request(MediaType.TEXT_PLAIN)
                                .post(Entity.entity(jose.sign("Hello"), MediaType.TEXT_PLAIN),
                                      String.class);
        Assert.assertEquals("Hello", jose.verify(signedData));
        Assert.assertEquals(3, signedData.split("\\.").length);
        VerificationOutput verification = jose.verification(signedData);
        Assert.assertEquals("HMacKey",verification.getHeaders().get("kid"));
    }
    
    @Test
    public void testJweDirectCompact() throws Exception {
        Jose jose = JoseLookup.lookup().get();
        String encryptedData = client.target("http://localhost:8080/encrypt")
                                .request(MediaType.TEXT_PLAIN)
                                .post(Entity.entity(jose.encrypt("Hello"), MediaType.TEXT_PLAIN),
                                      String.class);
        System.out.println(encryptedData);
        Assert.assertEquals(5, encryptedData.split("\\.").length);
        DecryptionOutput decryption = jose.decryption(encryptedData);
        Assert.assertEquals("Hello", jose.decrypt(encryptedData));
        Assert.assertEquals("AesGcmKey",decryption.getHeaders().get("kid"));
    }
}
