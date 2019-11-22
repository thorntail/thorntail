import org.apache.http.client.fluent.Request;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.swarm.jaxrs.JAXRSArchive;

import java.io.File;

import static org.junit.Assert.assertEquals;

@RunWith(Arquillian.class)
public class VerifyBuildArtefactIT {
    @Deployment
    public static Archive createDeployment() {
        return ShrinkWrap.createFromZipFile(JAXRSArchive.class, new File("target/endpoint.war"));
    }

    @Test
    @RunAsClient
    public void testEndppoint() throws Exception {
        String response = Request.Get("http://127.0.0.1:8080/").execute().returnContent().asString();
        assertEquals("something", response);
    }
}
