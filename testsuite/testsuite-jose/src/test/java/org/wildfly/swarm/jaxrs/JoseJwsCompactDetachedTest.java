package org.wildfly.swarm.jaxrs;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.swarm.jose.Jose;
import org.wildfly.swarm.jose.JoseLookup;

@RunWith(Arquillian.class)
public class JoseJwsCompactDetachedTest {

    @Deployment
    public static Archive<?> createDeployment() throws Exception {
        JAXRSArchive deployment = ShrinkWrap.create(JAXRSArchive.class);
        deployment.addResource(MyResource.class);
        deployment.addResource(SecuredApplication.class);
        deployment.addResource(JoseExceptionMapper.class); 
        deployment.addAllDependencies();
        deployment.addAsResource("keystore.jks");
        deployment.addAsResource("project-jws-compact-detached.yml", "project-defaults.yml");
        return deployment;
    }
    
    @Test
    @Ignore
    public void testJwsCompactUnencoded() throws Exception {
        Jose jose = JoseLookup.lookup().get();
        String signedData = ClientBuilder.newClient().target("http://localhost:8080/sign")
                                .request(MediaType.TEXT_PLAIN)
                                .post(Entity.entity(jose.sign("Hello"), MediaType.TEXT_PLAIN),
                                      String.class);
        Assert.assertEquals("Hello", jose.verify(signedData));
    }
       
}
