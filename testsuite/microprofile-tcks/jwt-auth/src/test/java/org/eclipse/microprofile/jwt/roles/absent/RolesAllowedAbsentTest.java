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
package org.eclipse.microprofile.jwt.roles.absent;

import static javax.ws.rs.core.MediaType.TEXT_PLAIN;

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
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 *
 * @author Martin Kouba
 */
public class RolesAllowedAbsentTest extends Arquillian {

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
        return TestArchive.createBase(RolesAllowedAbsentTest.class).addClasses(DenyAllClassLevel.class);
    }

    @BeforeClass
    public static void generateToken() throws Exception {
        token = TokenUtils.generateTokenString("/Token1.json", null, new HashMap<>());
    }

    @RunAsClient
    @Test
    public void testDenyAllClass() throws Exception {
        String uri = baseURL.toExternalForm() + "/denyAll";
        WebTarget echoEndpointTarget = ClientBuilder.newClient().target(uri).queryParam("input", "hello");
        Response response = echoEndpointTarget.request(TEXT_PLAIN).header(HttpHeaders.AUTHORIZATION, "Bearer " + token).get();
        Assert.assertEquals(response.getStatus(), HttpURLConnection.HTTP_FORBIDDEN);
    }

    @RunAsClient
    @Test
    public void testPermitAllMethod() throws Exception {
        String uri = baseURL.toExternalForm() + "/denyAll/permitAll";
        WebTarget echoEndpointTarget = ClientBuilder.newClient().target(uri).queryParam("input", "hello");
        Response response = echoEndpointTarget.request(TEXT_PLAIN).header(HttpHeaders.AUTHORIZATION, "Bearer " + token).get();
        Assert.assertEquals(response.getStatus(), HttpURLConnection.HTTP_OK);
        String reply = response.readEntity(String.class);
        // Must return hello, user={token upn claim}
        Assert.assertEquals(reply, "hello, user=jdoe@example.com");
    }

}
