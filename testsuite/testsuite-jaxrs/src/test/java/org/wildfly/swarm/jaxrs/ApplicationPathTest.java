package org.wildfly.swarm.jaxrs;

import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Request;
import org.apache.http.util.EntityUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

import static org.fest.assertions.Assertions.assertThat;

@RunWith(Arquillian.class)
public class ApplicationPathTest {
    @Deployment(testable = false)
    public static Archive createDeployment() throws Exception {
        return ShrinkWrap.create(JAXRSArchive.class, "myapp.war")
                .addAsResource("project-test-defaults-path.yml", "/project-defaults.yml")
                .addClass(MyResource.class)
                .addAllDependencies();
    }

    @Test
    @RunAsClient
    public void testChangeApplicationPath() throws IOException {
        HttpResponse response = Request.Get("http://localhost:8080/").execute().returnResponse();
        assertThat(response.getStatusLine().getStatusCode()).isEqualTo(200);
        assertThat(EntityUtils.toString(response.getEntity())).contains("Howdy at");
    }
}
