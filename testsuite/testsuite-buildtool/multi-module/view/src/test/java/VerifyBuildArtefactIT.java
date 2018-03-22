import java.io.File;
import java.net.URL;
import java.nio.charset.Charset;

import org.apache.commons.io.IOUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.junit.Assert;
import org.junit.runner.RunWith;
import org.wildfly.swarm.Swarm;
import org.wildfly.swarm.jaxrs.JAXRSArchive;

@RunWith(Arquillian.class)
public class VerifyBuildArtefactIT {


    @Deployment
    public static Archive createDeployment() throws Exception {
        try {
            return ShrinkWrap.createFromZipFile(JAXRSArchive.class, new File("target/endpoint.war"));
        } catch (Throwable e) {
            e.printStackTrace();
            throw e;
        }
    }

    @org.junit.Test
    public void testEndppoint() throws Exception {
        String urlContents = IOUtils.toString(new URL("http://127.0.0.1:8080/"), Charset.forName("UTF-8"));
        Assert.assertEquals("something", urlContents);
    }

    private static Swarm container;
}
