package org.wildfly.swarm.jsp.test;

import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Request;
import org.apache.http.util.EntityUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.swarm.undertow.WARArchive;

@RunWith(Arquillian.class)
public class ArquillianTest {

    @Deployment
    public static Archive createDeployment() throws Exception {
        WARArchive deployment = ShrinkWrap.create(WARArchive.class, "services.war");
        deployment.addClass(ServicesServlet.class);
        deployment.addClass(TransformerServlet.class);
        return deployment;
    }

    @Test
    @RunAsClient
    public void testServices() throws Exception {
        HttpResponse response = Request.Get("http://localhost:8080/services").execute().returnResponse();
        String responseBody = EntityUtils.toString(response.getEntity());
        System.out.println(responseBody);
        Assert.assertEquals(200, response.getStatusLine().getStatusCode());
        Assert.assertTrue(responseBody.startsWith("No TransformerFactory could be found!"));
    }

}
