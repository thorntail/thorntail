/*
 *   Copyright 2018 Red Hat, Inc, and individual contributors.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */
package org.eclipse.microprofile.jwt.roles.implicit;

import static javax.ws.rs.core.MediaType.TEXT_PLAIN;
import static org.testng.Assert.assertEquals;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import org.eclipse.microprofile.jwt.TestArchive;
import org.eclipse.microprofile.jwt.tck.util.TokenUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.arquillian.testng.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wildfly.swarm.microprofile.jwtauth.MicroProfileJWTAuthFraction;

/**
 * Test that if there is no class-level security annotation and <code>thorntail.microprofile.jwt.default-missing-method-permissions-deny-access</code> is set to
 * false an unconstrained resource method is skipped.
 *
 * @author Martin Kouba
 * @see MicroProfileJWTAuthFraction#isDefaultMissingMethodPermissionsDenyAccess()
 */
public class NoClassLevelUnconstrainedSkippedTest extends Arquillian {

    /**
     * The test generated JWT token string
     */
    private static String token;

    /**
     * The base URL for the container under test
     */
    @ArquillianResource
    private URL baseURL;

    @Deployment
    public static WebArchive createDeployment() {
        return TestArchive.createBase(NoClassLevelUnconstrainedSkippedTest.class)
                .addAsResource("project-defaults-skip-unconstrained-method.yml", "/project-defaults.yml")
                .addClasses(NoClassLevelWithUnconstrainedMethodEndpoint.class);
    }

    @BeforeClass
    public static void generateToken() throws Exception {
        token = TokenUtils.generateTokenString("/Token1.json", null, new HashMap<>());
    }

    @RunAsClient
    @Test
    public void testUnconstrainedSkipped() throws Exception {
        String uri = baseURL.toExternalForm() + "/endpoint";
        WebTarget echoEndpointTarget = ClientBuilder.newClient().target(uri);
        Response response = echoEndpointTarget.request(TEXT_PLAIN).get();
        assertEquals(response.getStatus(), HttpURLConnection.HTTP_OK);
        String reply = response.readEntity(String.class);
        assertEquals(reply, "OK");
    }

    @RunAsClient
    @Test
    public void testRolesAllowedMethod() throws Exception {
        String uri = baseURL.toExternalForm() + "/endpoint/echo";
        WebTarget echoEndpointTarget = ClientBuilder.newClient().target(uri).queryParam("input", "hello");
        Response response = echoEndpointTarget.request(TEXT_PLAIN).header(HttpHeaders.AUTHORIZATION, "Bearer " + token).get();
        assertEquals(response.getStatus(), HttpURLConnection.HTTP_OK);
        String reply = response.readEntity(String.class);
        // Must return hello, user={token upn claim}
        assertEquals(reply, "hello, user=jdoe@example.com");
    }

}
