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

import java.io.File;
import java.net.URL;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.HttpHeaders;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.ClassLoaderAsset;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.swarm.Swarm;
import org.wildfly.swarm.arquillian.CreateSwarm;
import org.wildfly.swarm.jaxrs.JAXRSArchive;
import org.wildfly.swarm.microprofile.jwtauth.keycloak.SecuredApplication;
import org.wildfly.swarm.microprofile.jwtauth.keycloak.SecuredResource;
import org.wildfly.swarm.microprofile.jwtauth.keycloak.deployment.principal.KeycloakJWTCallerPrincipal;
import org.wildfly.swarm.microprofile.jwtauth.keycloak.deployment.principal.KeycloakJWTCallerPrincipalFactory;

import io.smallrye.jwt.auth.principal.JWTCallerPrincipalFactory;

/**
 * @author Bob McWhirter
 */
@SuppressWarnings("deprecation")
@RunWith(Arquillian.class)
public class KeycloakMicroprofileJwtTest {

    @Deployment
    public static Archive<?> createDeployment() throws Exception {
        JAXRSArchive deployment = ShrinkWrap.create(JAXRSArchive.class, "test.war");
        deployment.addClass(SecuredApplication.class);
        deployment.addClass(SecuredResource.class);
        deployment.addResource(JWTCallerPrincipalFactory.class);
        deployment.addResource(KeycloakJWTCallerPrincipalFactory.class);
        deployment.addResource(KeycloakJWTCallerPrincipal.class);
        deployment.addAsResource("wildfly-swarm-keycloak-example-realm.json");
        deployment.addAsResource("keycloak.json");
        deployment.addAsResource("project-no-roles-props.yml", "project-defaults.yml");
        deployment.addAsServiceProvider(JWTCallerPrincipalFactory.class, KeycloakJWTCallerPrincipalFactory.class);
        String[] deps = {
                "org.keycloak:keycloak-core",
                "org.keycloak:keycloak-adapter-core",
                "org.keycloak:keycloak-admin-cli",
                //TODO: remove
                "org.bitbucket.b_c:jose4j"
        };
        File[] dependencies = Maven.resolver().loadPomFromFile(new File("pom.xml")).resolve(deps).withTransitivity().asFile();
        deployment.addAsLibraries(dependencies);
        
        //TODO: remove, there should be no need to include it with KC keycloak.json providing all the info
        deployment.addAsManifestResource(new ClassLoaderAsset("keys/public-key.pem"), "/MP-JWT-SIGNER");
        return deployment;
    }

    // For some reason doing this in a static or @BeforeClass doesn't work
    @CreateSwarm
    public static Swarm newContainer() throws Exception {
        URL migrationRealmUrl = KeycloakMicroprofileJwtTest.class.getResource("/wildfly-swarm-keycloak-example-realm.json");
        System.setProperty("keycloak.migration.file", migrationRealmUrl.toURI().getPath());
        System.setProperty("keycloak.migration.provider", "singleFile");
        System.setProperty("keycloak.migration.action", "import");
        return new Swarm();
    }

    @Test
    @RunAsClient
    public void testResourceIsSecured() throws Exception {
        final String tokenUri =
            "http://localhost:8080/auth/realms/thorntail-cmd-client/protocol/openid-connect/token";
        String response =
            ClientBuilder.newClient().target(tokenUri).request()
                .post(Entity.form(
                        new Form().param("grant_type", "password").param("client_id", "thorntail-cmd-client-example")
                                  .param("username", "user1").param("password", "password1")),
                        String.class);
        String accessToken = getAccessTokenFromResponse(response);

        String serviceResponse =
            ClientBuilder.newClient().target("http://localhost:8080/mpjwt/secured")
                .request()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .get(String.class);
        Assert.assertEquals("Hi user1, this resource is secured", serviceResponse);

    }

    private String getAccessTokenFromResponse(String response) {
        String tokenStart = response.substring("{\"access_token\":\"".length());
        return tokenStart.substring(0, tokenStart.indexOf("\""));
    }
}
