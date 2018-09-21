package org.wildfly.swarm.mvc.test;

import org.apache.http.client.fluent.Content;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.ClassLoaderAsset;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;

import org.wildfly.swarm.jaxrs.JAXRSArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.net.URL;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(Arquillian.class)
public class MvcTest {

    @ArquillianResource
    private URL baseUrl;

    @Deployment(testable = false)
    public static Archive createDeployment() {
        return ShrinkWrap.create(JAXRSArchive.class, "myapp.war")
                .addClass(MvcApplication.class)
                .addClass(MvcController.class)
                .addAsWebInfResource(new ClassLoaderAsset("views/hello.jsp"), "views/hello.jsp")
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Test
    @RunAsClient
    public void testGettingView() throws IOException {
        Response response = Request.Get(baseUrl.toString() + "/hello").execute();
        Content content = response.returnContent();
        assertThat(content.asString(), containsString("Hello World!"));
    }
}
