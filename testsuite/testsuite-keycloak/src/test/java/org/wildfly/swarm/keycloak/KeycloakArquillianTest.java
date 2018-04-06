/**
 * Copyright 2015-2016 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wildfly.swarm.keycloak;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.net.URL;
import java.util.UUID;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.swarm.Swarm;
import org.wildfly.swarm.arquillian.CreateSwarm;
import org.wildfly.swarm.jaxrs.JAXRSArchive;

/**
 * @author Bob McWhirter
 */
@RunWith(Arquillian.class)
public class KeycloakArquillianTest {

    @Deployment
    public static Archive<?> createDeployment() throws Exception {
        JAXRSArchive deployment = ShrinkWrap.create(JAXRSArchive.class);
        deployment.addResource(SecuredApplication.class);
        deployment.addResource(SecuredResource.class);
        deployment.addAsResource("wildfly-swarm-keycloak-example-realm.json");
        deployment.addAsResource("keycloak.json");
        return deployment;
    }

    @CreateSwarm
    public static Swarm newContainer() throws Exception {
        String warName = getWarName();
        File file = File.createTempFile("project-defaults", ".yaml");
        file.deleteOnExit();
        createSwarmConfiguration(file, warName);

        URL keyCloakJsonUrl = KeycloakArquillianTest.class.getResource("/keycloak.json");
        System.setProperty("swarm.keycloak.json.path", stripLeadingSlashIfWin(keyCloakJsonUrl.toURI().getPath()));
        URL migrationRealmUrl = KeycloakArquillianTest.class.getResource("/wildfly-swarm-keycloak-example-realm.json");
        System.setProperty("keycloak.migration.file", stripLeadingSlashIfWin(migrationRealmUrl.toURI().getPath()));
        System.setProperty("keycloak.migration.provider", "singleFile");
        System.setProperty("keycloak.migration.action", "import");
        return new Swarm("-s" + stripLeadingSlashIfWin(file.toURI().getPath()));
    }

    private static String stripLeadingSlashIfWin(String path) {
        return System.getProperty("os.name").toLowerCase().indexOf("win") >= 0 && path.startsWith("/") 
            ? path.substring(1) : path;
    }

    @Test
    @RunAsClient
    public void testResourceIsSecured() throws Exception {
        // Check 401 is returned without the token
        Assert.assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(),
           ClientBuilder.newClient().target("http://localhost:8080/secured").request().get().getStatus());
        
        final String tokenUri = 
            "http://localhost:8080/auth/realms/wildfly-swarm-keycloak-example/protocol/openid-connect/token";
        String response = 
            ClientBuilder.newClient().target(tokenUri).request()
                .post(Entity.form(
                        new Form().param("grant_type", "password").param("client_id", "curl")
                                  .param("username", "user1").param("password", "password1")),
                        String.class);
        String accessToken = getAccessTokenFromResponse(response);
        
        String serviceResponse = 
            ClientBuilder.newClient().target("http://localhost:8080/secured")
                .request()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .get(String.class);
        Assert.assertEquals("Hi user1, this resource is secured", serviceResponse);

    }

    private String getAccessTokenFromResponse(String response) {
        String tokenStart = response.substring("{\"access_token\":\"".length());
        return tokenStart.substring(0, tokenStart.indexOf("\""));
    }

    private static String getWarName() throws Exception {
        // Arquillian creates a war using a "wfswarm" + UUID + ".war" format.
        
        String webInfPath = KeycloakArquillianTest.class.getResource("/WEB-INF/").toURI().toString();
        String warPath = webInfPath.substring(0, webInfPath.length() - "/WEB-INF/".length());
        String warName = warPath.substring(warPath.lastIndexOf('/') + 1);
        warName = warName.substring("wfswarm".length());
        return warName.substring(0, UUID.randomUUID().toString().length()) + ".war";
    }
    
    private static void createSwarmConfiguration(File file, String warName) throws Exception {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write("swarm:");
            writer.newLine();
            writer.write("  deployment:");
            writer.newLine();
            writer.write("    " + warName + ":");
            writer.newLine();
            writer.write("      web:");
            writer.newLine();
            writer.write("        login-config:");
            writer.newLine();
            writer.write("          auth-method: KEYCLOAK");
            writer.newLine();
            writer.write("        security-constraints:");
            writer.newLine();
            writer.write("          - url-pattern: /secured");
            writer.newLine();
            writer.write("            methods: [GET]");
            writer.newLine();
            writer.write("            roles: [admin]");
        }
    }
}
