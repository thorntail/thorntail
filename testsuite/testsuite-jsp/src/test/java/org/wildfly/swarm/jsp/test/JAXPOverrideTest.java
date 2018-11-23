package org.wildfly.swarm.jsp.test;

import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Request;
import org.apache.http.util.EntityUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.swarm.jsp.ServicesServlet;
import org.wildfly.swarm.jsp.TransformerServlet;
import org.wildfly.swarm.undertow.WARArchive;

@RunWith(Arquillian.class)
public class JAXPOverrideTest {

    @Deployment
    public static Archive createDeployment() throws Exception {
        WARArchive deployment = ShrinkWrap.create(WARArchive.class, "services.war");
        deployment.addAsLibraries(Maven.resolver().resolve("saxon:saxon:8.7").withTransitivity().asFile());
        deployment.addClass(ServicesServlet.class);
        deployment.addClass(TransformerServlet.class);
        return deployment;
    }

    @Test
    @RunAsClient
    public void testFactory() throws Exception {
        HttpResponse response = Request.Get("http://localhost:8080/transformer").execute().returnResponse();
        String responseBody = EntityUtils.toString(response.getEntity());
        System.out.println(responseBody);
        Assert.assertTrue(responseBody.startsWith("net.sf.saxon.TransformerFactoryImpl"));
    }
}
