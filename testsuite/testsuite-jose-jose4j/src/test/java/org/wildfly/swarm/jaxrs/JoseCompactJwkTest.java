package org.wildfly.swarm.jaxrs;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;

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
public class JoseCompactJwkTest {

    @Deployment
    public static Archive<?> createDeployment() throws Exception {
        JAXRSArchive deployment = ShrinkWrap.create(JAXRSArchive.class);
        deployment.addResource(MyResource.class);
        deployment.addResource(SecuredApplication.class);
        deployment.addResource(Jose4jJoseFactory.class);
        deployment.addResource(Jose4jJoseImpl.class);  
        deployment.addAllDependencies();
        deployment.addAsResource("jwk.keys");
        deployment.addAsResource("project-jwk-store.yml", "project-defaults.yml");
        deployment.addAsServiceProvider(JoseFactory.class, Jose4jJoseFactory.class);
        return deployment;
    }
    
    @Test
    public void testJwsHmacCompact() throws Exception {
        Jose jose = getJose();
        String signedData = ClientBuilder.newClient().target("http://localhost:8080/sign")
                                .request(MediaType.TEXT_PLAIN)
                                .post(Entity.entity(jose.sign("Hello"), MediaType.TEXT_PLAIN),
                                      String.class);
        Assert.assertEquals("Hello", jose.verify(signedData));
        Assert.assertEquals(3, signedData.split("\\.").length);
    }
    
    @Test
    public void testJweDirectCompact() throws Exception {
        Jose jose = getJose();
        String encryptedData = ClientBuilder.newClient().target("http://localhost:8080/encrypt")
                                .request(MediaType.TEXT_PLAIN)
                                .post(Entity.entity(jose.encrypt("Hello"), MediaType.TEXT_PLAIN),
                                      String.class);
        Assert.assertEquals(5, encryptedData.split("\\.").length);
        Assert.assertEquals("Hello", jose.decrypt(encryptedData));
    }

    private Jose getJose() throws Exception {
        Jose jose = JoseLookup.lookup().get();
        Assert.assertEquals("Jose4jJoseImpl", jose.getClass().getSimpleName());
        return jose;
    }
}
