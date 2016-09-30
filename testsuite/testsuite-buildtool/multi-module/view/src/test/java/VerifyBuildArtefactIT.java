import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

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
        String urlContents = getUrlContents("http://127.0.0.1:8080/");
        Assert.assertEquals("something\n", urlContents);
    }

    private static String getUrlContents(String theUrl) {
        StringBuilder content = new StringBuilder();

        try {
            URL url = new URL(theUrl);
            URLConnection urlConnection = url.openConnection();
            BufferedReader bufferedReader = new BufferedReader(
                    new InputStreamReader(urlConnection.getInputStream())
            );

            String line;

            while ((line = bufferedReader.readLine()) != null) {
                content.append(line + "\n");
            }
            bufferedReader.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return content.toString();
    }

    private static Swarm container;
}
