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

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.HttpHeaders;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.swarm.jaxrs.JAXRSArchive;
import org.wildfly.swarm.microprofile.jwtauth.keycloak.SecuredApplication;
import org.wildfly.swarm.microprofile.jwtauth.keycloak.SecuredResource;

@RunWith(Arquillian.class)
public class KeycloakMicroprofileJwtTest {

    @Deployment
    public static Archive<?> createDeployment() {
        JAXRSArchive deployment = ShrinkWrap.create(JAXRSArchive.class, "test.war");
        deployment.addClass(SecuredApplication.class);
        deployment.addClass(SecuredResource.class);
        deployment.addAsResource("keycloak.json");
        deployment.addAsResource("project-defaults.yml");
        return deployment;
    }

    @Test
    @RunAsClient
    public void testResourceIsSecured() {
        String authResponse = ClientBuilder.newClient()
                .target("http://localhost:8080/auth/realms/thorntail-cmd-client/protocol/openid-connect/token")
                .request()
                .post(Entity.form(new Form()
                        .param("grant_type", "password")
                        .param("client_id", "thorntail-cmd-client-example")
                        .param("username", "user1")
                        .param("password", "password1")
                ), String.class);
        String accessToken = getAccessTokenFromResponse(authResponse);

        String serviceResponse = ClientBuilder.newClient()
                .target("http://localhost:8080/mpjwt/secured")
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
