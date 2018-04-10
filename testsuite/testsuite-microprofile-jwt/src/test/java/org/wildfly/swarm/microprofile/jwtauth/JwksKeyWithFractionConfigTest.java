package org.wildfly.swarm.microprofile.jwtauth;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.swarm.Swarm;
import org.wildfly.swarm.arquillian.CreateSwarm;
import org.wildfly.swarm.jaxrs.JAXRSArchive;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@RunWith(Arquillian.class)
public class JwksKeyWithFractionConfigTest {
    private static JwksServer jwksServer;

    @AfterClass
    public static void after() {
        jwksServer.stop();
    }

    @BeforeClass
    public static void before() {
        try {
            jwksServer = new JwksServer(
                    KeyTool.newKeyTool(JwksKeyWithFractionConfigTest.class.getResource("/pkcs8_good_key.pem").toURI()),
                    14145);
            jwksServer.start();
        } catch (final Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    @Deployment(testable = false)
    public static JAXRSArchive createDeployment() {
        return ShrinkWrap.create(JAXRSArchive.class)
                .addClass(TestApplication.class)
                .addClass(TokenResource.class)
                .addClass(KeyTool.class)
                .addClass(JwtTool.class)
                .addAsResource("project-defaults.yml")
                .addAsResource("emptyRoles.properties")
                .addAsResource("pkcs8_bad_key.pem")
                .addAsResource("pkcs8_good_key.pem")
                .setContextRoot("/testsuite");
    }

    @CreateSwarm
    public static Swarm newContainer() throws Exception {
        return new Swarm()
                .withProperty("swarm.microprofile.jwtauth.token.jwksUri", "http://localhost:14145/jwks")
                .withProperty("swarm.microprofile.jwtauth.token.issuedBy", "http://testsuite-jwt-issuer.io");
    }

    @Test
    @RunAsClient
    public void testThatJwksKeyIsVerified() throws Exception {
        final KeyTool keyTool = KeyTool.newKeyTool(getClass().getResource("/pkcs8_good_key.pem").toURI());
        final String jwt = new JwtTool(keyTool, "http://testsuite-jwt-issuer.io").generateSignedJwt();
        final URL url = new URL("http://localhost:8080/testsuite/mpjwt/token");
        final URLConnection urlConnection = url.openConnection();
        urlConnection.addRequestProperty("Authorization", "Bearer " + jwt);
        try (InputStreamReader isr = new InputStreamReader(urlConnection.getInputStream());
             BufferedReader br = new BufferedReader(isr)) {
            assertEquals(jwt, br.readLine());
        }
    }

    @Test
    @RunAsClient
    public void testThatJwksKeyIsFake() throws Exception {
        final KeyTool keyTool = KeyTool.newKeyTool(getClass().getResource("/pkcs8_bad_key.pem").toURI());
        final String jwt = new JwtTool(keyTool, "http://testsuite-jwt-issuer.io").generateSignedJwt();
        final URL url = new URL("http://localhost:8080/testsuite/mpjwt/token");
        final URLConnection urlConnection = url.openConnection();
        urlConnection.addRequestProperty("Authorization", "Bearer " + jwt);
        try (InputStreamReader isr = new InputStreamReader(urlConnection.getInputStream());
             BufferedReader br = new BufferedReader(isr)) {
            assertNull(br.readLine()); // only if no body is returned, we know that the JWT was refused.
        }
    }
}
