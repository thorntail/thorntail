package org.wildfly.swarm.jaxrs;

import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.resteasy.plugins.providers.jsonb.JsonBindingProvider;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class JaxrsJsonbTest {

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
        deployment.addResource(MyApplication.class);
        deployment.addResource(Dog.class); 
        deployment.addAllDependencies();
        return deployment;
    }

    @Test
    @RunAsClient
    public void testJaxrsJsonb() throws Exception {
        Dog dog = new Dog();
        dog.name = "Falco";
        dog.age = 4;
        dog.bitable = false;

        Jsonb jsonb = JsonbBuilder.create();
        
        String response = client.target("http://localhost:8080/jsonb")
                              .request(MediaType.APPLICATION_JSON)
                              .post(Entity.entity(jsonb.toJson(dog), MediaType.APPLICATION_JSON),
                              String.class);
        Dog dog2 = jsonb.fromJson(response, Dog.class);
        Assert.assertEquals(dog.name, dog2.name);
        Assert.assertEquals(dog.age, dog2.age);
        Assert.assertTrue(dog2.bitable);
    }

    @Test
    @RunAsClient
    public void testJaxrsJsonbWithProvider() throws Exception {
        Dog dog = new Dog();
        dog.name = "Falco";
        dog.age = 4;
        dog.bitable = false;

        Dog dog2 = client.target("http://localhost:8080/jsonb")
                       .register(new JsonBindingProvider())
                       .request(MediaType.APPLICATION_JSON)
                       .post(Entity.entity(dog, MediaType.APPLICATION_JSON),
                       Dog.class);
        
        Assert.assertEquals(dog.name, dog2.name);
        Assert.assertEquals(dog.age, dog2.age);
        Assert.assertTrue(dog2.bitable);
    }
    
}
