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
import org.wildfly.swarm.jose.jose4j.Jose4jJoseFactory;
import org.wildfly.swarm.jose.jose4j.Jose4jJoseImpl;
import org.wildfly.swarm.jose.provider.JoseFactory;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;

@RunWith(Arquillian.class)
public class JoseCompactJwkInOutTest {

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
        deployment.addResource(Jose4jJoseFactory.class);
        deployment.addResource(Jose4jJoseImpl.class);
        deployment.addAllDependencies();
        deployment.addAsResource("jwk.keys");
        deployment.addAsResource("project-jwk-store-in-out.yml", "project-defaults.yml");
        deployment.addAsServiceProvider(JoseFactory.class, Jose4jJoseFactory.class);
        return deployment;
    }

    @Test
    public void testJweEncryptDecryption() throws Exception {
        Jose jose = JoseLookup.lookup().get();
        String encryptedData = jose.encrypt("payload");
        DecryptionOutput decryptionOutput = jose.decryption(encryptedData);
        Assert.assertEquals(5, encryptedData.split("\\.").length);
        Assert.assertEquals("2", decryptionOutput.getHeaders().get("kid"));
        Assert.assertEquals("payload", jose.decryption(encryptedData).getData());
    }
}
