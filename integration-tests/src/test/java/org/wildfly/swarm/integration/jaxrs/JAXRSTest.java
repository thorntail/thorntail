package org.wildfly.swarm.integration.jaxrs;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.swarm.container.Container;
import org.wildfly.swarm.jaxrs.JAXRSArchive;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author Bob McWhirter
 */
//@RunWith(Arquillian.class)
public class JAXRSTest  {

    @Deployment
    public static Archive createDeployment() throws Exception {
        JAXRSArchive deployment = ShrinkWrap.create(JAXRSArchive.class);
        deployment.addResource(MyResource.class);
        deployment.addAllDependencies();
        deployment.staticContent();
        return deployment;
    }

//    @RunAsClient @Test
    public void testSimple() throws IOException {
        assertThat(fetch("http://localhost:8080/")).contains("Howdy at");
        assertThat(fetch("http://localhost:8080/static-content.txt")).contains("This is static.");
    }

    protected String fetch(String urlStr) throws IOException {
        URL url = new URL(urlStr);
        StringBuffer buffer = new StringBuffer();
        try (InputStream in = url.openStream()) {
            int numRead = 0;
            while (numRead >= 0) {
                byte[] b = new byte[1024];
                numRead = in.read(b);
                if (numRead < 0) {
                    break;
                }
                buffer.append(new String(b, 0, numRead));
            }
        }

        return buffer.toString();
    }

}
