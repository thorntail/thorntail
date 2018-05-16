package org.wildfly.swarm.jaxrs;

import java.util.Base64;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.swarm.jose.Jose;
import org.wildfly.swarm.jose.JoseLookup;
import org.wildfly.swarm.jose.jose4j.Jose4jJoseFactory;
import org.wildfly.swarm.jose.jose4j.Jose4jJoseImpl;
import org.wildfly.swarm.jose.provider.JoseFactory;

@RunWith(Arquillian.class)
public class JoseCompactTest {

    @Deployment
    public static Archive<?> createDeployment() throws Exception {
        JAXRSArchive deployment = ShrinkWrap.create(JAXRSArchive.class);
        deployment.addResource(MyResource.class);
        deployment.addResource(SecuredApplication.class);
        deployment.addResource(JoseExceptionMapper.class);
        deployment.addResource(Jose4jJoseFactory.class);
        deployment.addResource(Jose4jJoseImpl.class);  
        deployment.addAllDependencies();
        deployment.addAsResource("keystore.jks");
        deployment.addAsResource("project-jose-compact.yml", "project-defaults.yml");
        deployment.addAsServiceProvider(JoseFactory.class, Jose4jJoseFactory.class);
        return deployment;
    }
    
    @Test
    public void testJwsCompact() throws Exception {
        Jose jose = getJose();
        String signedData = ClientBuilder.newClient().target("http://localhost:8080/sign")
                                .request(MediaType.TEXT_PLAIN)
                                .post(Entity.entity(jose.sign("Hello"), MediaType.TEXT_PLAIN),
                                      String.class);
        Assert.assertEquals("Hello", jose.verify(signedData));
        Assert.assertEquals(3, signedData.split("\\.").length);
    }
    
    @Test
    public void testJwsCompactTampered() throws Exception {
        String[] jwsParts = getJose().sign("Hello").split("\\.");
        Assert.assertEquals(3, jwsParts.length);
        String encodedContent = jwsParts[1];
        Assert.assertEquals("Hello", new String(Base64.getUrlDecoder().decode(encodedContent)));
        String newEncodedContent = new String(Base64.getUrlEncoder().encode("HellO".getBytes()));
        Assert.assertEquals("HellO", new String(Base64.getUrlDecoder().decode(newEncodedContent)));
        // Headers + content + signature
        String newJws = jwsParts[0] + "." + newEncodedContent + "." + jwsParts[2];  
        Assert.assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), 
            ClientBuilder.newClient().target("http://localhost:8080/sign")
               .request(MediaType.TEXT_PLAIN)
               .post(Entity.entity(newJws, MediaType.TEXT_PLAIN))
               .getStatus());
    }
       
    @Test
    public void testJwsJweCompact() throws Exception {
        Jose jose = getJose();
        String signedAndEncryptedData = ClientBuilder.newClient().target("http://localhost:8080/signAndEncrypt")
                                   .request(MediaType.TEXT_PLAIN)
                                   .post(Entity.entity(jose.encrypt(jose.sign("Hello")), MediaType.TEXT_PLAIN),
                                         String.class);
        String signedData = jose.decrypt(signedAndEncryptedData);
        Assert.assertEquals("Hello", jose.verify(signedData));
        
        Assert.assertEquals(3, signedData.split("\\.").length);
        Assert.assertEquals(5, signedAndEncryptedData.split("\\.").length);
    }
    
    @Test
    public void testJweCompact() throws Exception {
        Jose jose = getJose();
        String encryptedData = ClientBuilder.newClient().target("http://localhost:8080/encrypt")
                                   .request(MediaType.TEXT_PLAIN)
                                   .post(Entity.entity(jose.encrypt("Hello"), MediaType.TEXT_PLAIN),
                                         String.class);
        Assert.assertEquals("Hello", jose.decrypt(encryptedData));
        Assert.assertEquals(5, encryptedData.split("\\.").length);
    }
    
    @Test
    public void testJweCompactTampered() throws Exception {
        Jose jose = getJose();
        String[] jweParts = jose.encrypt("Hello").split("\\.");
        Assert.assertEquals(5, jweParts.length);
        String[] newJweParts = jose.encrypt("HellO").split("\\.");
        // Headers + IV + Encrypted CEK + Cipher + Authentication Tag 
        String newJwe = jweParts[0] + "." + jweParts[1] + "." + jweParts[2] + "."
                + newJweParts[3] + "." + jweParts[4]; 
        
        Assert.assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), 
            ClientBuilder.newClient().target("http://localhost:8080/encrypt")
               .request(MediaType.TEXT_PLAIN)
               .post(Entity.entity(newJwe, MediaType.TEXT_PLAIN))
               .getStatus());
    }
    
    private Jose getJose() throws Exception {
        Jose jose = JoseLookup.lookup().get();
        Assert.assertEquals("Jose4jJoseImpl", jose.getClass().getSimpleName());
        return jose;
    }
}
