package org.wildfly.swarm.webservices;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.ClassLoaderAsset;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.swarm.undertow.WARArchive;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author Ken Finnigan
 */
@RunWith(Arquillian.class)
public class SWARM582Test {
    private static final String MESSAGE = "Mickey Mouse";

    @Deployment(testable = false)
    public static Archive deployment() {
        WARArchive deployment = ShrinkWrap.create(WARArchive.class, "wstest.war")
                .addClass(EchoClientServlet.class)
                .addClass(EchoClientWithHandlerServlet.class)
                .addClass(EchoService.class)
                .addClass(EchoServiceImpl.class)
                .addClass(EchoServiceClient.class)
                .addClass(EchoServiceClientWithHandler.class)
                .addAsWebInfResource(new ClassLoaderAsset("handler-chain.xml"), "classes/org/wildfly/swarm/webservices/handler-chain.xml")
                .addClass(MySOAPHandler.class)
                .addAsResource("project-defaults.yml");

        return deployment;
    }

    @Test
    @RunAsClient
    public void testServletAsWebServiceClient() throws Exception {
        URL url = new URL("http://localhost:8080/client?message=" + URLEncoder.encode(MESSAGE, StandardCharsets.UTF_8.name()));
        final String response = readResponse(url);

        assertThat(response).isEqualTo("ECHO:" + MESSAGE);
    }

    @Test
    @RunAsClient
    public void testServletAsWebServiceClientWithHandler() throws Exception {
        URL url = new URL("http://localhost:8080/clientWithHandler?message=" + URLEncoder.encode(MESSAGE, StandardCharsets.UTF_8.name()));
        final String response = readResponse(url);

        assertThat(response).isEqualTo("ECHO:" + MESSAGE);
    }

    private String readResponse(URL url) throws IOException {
        final InputStream stream = url.openStream();

        try (final BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
            return reader.lines().collect(Collectors.joining("\n"));
        }
    }

}
