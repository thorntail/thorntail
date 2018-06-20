package jwt;

import java.io.IOException;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.eclipse.microprofile.jwt.Claims;
import org.eclipse.microprofile.jwt.tck.util.TokenUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.arquillian.testng.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Tests that the {@linkplain java.security.Principal} can be used as an injection type
 * for the authenticated user
 */
public class PrincipalInjectionTest extends Arquillian {

    /**
     * The test generated JWT token string
     */
    private static String token;
    // Time claims in the token
    private static Long authTimeClaim;

    /**
     * The base URL for the container under test
     */
    @ArquillianResource
    private URL baseURL;

    /**
     * Create a CDI aware base web application archive
     * @return the base base web application archive
     * @throws IOException - on resource failure
     */
    @Deployment(testable=true)
    public static WebArchive createDeployment() throws IOException {
        URL publicKey = PrincipalInjectionEndpoint.class.getResource("/publicKey.pem");
        WebArchive webArchive = ShrinkWrap
                .create(WebArchive.class, "PrincipalInjectionEndpoint.war")
                .addAsResource(publicKey, "/publicKey.pem")
                .addAsResource("project-defaults.yml", "/project-defaults.yml")
                .addClass(PrincipalInjectionEndpoint.class)
                .addClass(TestApplication.class)
                .addAsManifestResource("microprofile-config.properties", "microprofile-config.properties")
                .addAsWebInfResource("beans.xml", "beans.xml")
                ;
        System.out.printf("WebArchive: %s\n", webArchive.toString(true));
        return webArchive;
    }

    @RunAsClient
    @Test()
    public void verifyInjectedPrincipal() throws Exception {
        HashMap<String, Long> timeClaims = new HashMap<>();
        token = TokenUtils.generateTokenString("/Token1.json", null, timeClaims);
        authTimeClaim = timeClaims.get(Claims.auth_time.name());

        String uri = baseURL.toExternalForm() + "endp/verifyInjectedPrincipal";
        WebTarget echoEndpointTarget = ClientBuilder.newClient()
                .target(uri);
        Response response = echoEndpointTarget.request(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, "Bearer " + token).get();
        Assert.assertEquals(response.getStatus(), HttpURLConnection.HTTP_OK);
        String replyString = response.readEntity(String.class);
        JsonReader jsonReader = Json.createReader(new StringReader(replyString));
        JsonObject reply = jsonReader.readObject();
        System.out.println(reply.toString());
        Assert.assertTrue(reply.getBoolean("pass"), reply.getString("msg"));
    }

}
