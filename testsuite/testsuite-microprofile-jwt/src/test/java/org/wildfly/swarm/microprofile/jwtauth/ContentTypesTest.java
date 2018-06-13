/**
 * Copyright 2018 Red Hat, Inc, and individual contributors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wildfly.swarm.microprofile.jwtauth;

import org.apache.http.client.fluent.Request;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.ClassLoaderAsset;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.swarm.microprofile.jwtauth.roles.TestApplication;
import org.wildfly.swarm.undertow.WARArchive;

import javax.ws.rs.core.MediaType;

import static org.wildfly.swarm.microprofile.jwtauth.utils.TokenUtils.createToken;

/**
 * @author Michal Szynkiewicz, michal.l.szynkiewicz@gmail.com
 * <br>
 * Date: 6/13/18
 */
@RunWith(Arquillian.class)
public class ContentTypesTest {

    @Deployment
    public static Archive<?> createDeployment() throws Exception {
        return initDeployment().addAsResource("project-no-roles-props.yml", "project-defaults.yml");
    }
    
    protected static WARArchive initDeployment() throws Exception {
        WARArchive deployment = ShrinkWrap.create(WARArchive.class);
        deployment.addClass(ContentTypesResource.class);
        deployment.addClass(TestApplication.class);
        deployment.addAsManifestResource(new ClassLoaderAsset("keys/public-key.pem"), "/MP-JWT-SIGNER");
        return deployment;
    }

    @RunAsClient
    @Test
    public void shouldGetHtmlForAllowedUser() throws Exception {
        String response = Request.Get("http://localhost:8080/mpjwt/content-types")
                .setHeader("Authorization", "Bearer " + createToken("MappedRole2"))
                .setHeader("Accept", MediaType.TEXT_HTML)
                .execute().returnContent().asString();
        Assert.assertEquals(ContentTypesResource.HTML_RESPONSE, response);
    }

    @RunAsClient
    @Test
    public void shouldNotGetHtmlForForbiddenUser() throws Exception {
        int statusCode = Request.Get("http://localhost:8080/mpjwt/content-types")
                .setHeader("Authorization", "Bearer " + createToken("MappedRole"))
                .setHeader("Accept", MediaType.TEXT_HTML)
                .execute().returnResponse().getStatusLine().getStatusCode();
        Assert.assertEquals(403, statusCode);
    }
    @RunAsClient
    @Test
    public void shouldGetPlainForAllowedUser() throws Exception {
        String response = Request.Get("http://localhost:8080/mpjwt/content-types")
                .setHeader("Authorization", "Bearer " + createToken("MappedRole"))
                .setHeader("Accept", MediaType.TEXT_PLAIN)
                .execute().returnContent().asString();
        Assert.assertEquals(ContentTypesResource.PLAIN_RESPONSE, response);
    }

    @RunAsClient
    @Test
    public void shouldNotGetPlainForForbiddenUser() throws Exception {
        int statusCode = Request.Get("http://localhost:8080/mpjwt/content-types")
                .setHeader("Authorization", "Bearer " + createToken("MappedRole2"))
                .setHeader("Accept", MediaType.TEXT_PLAIN)
                .execute().returnResponse().getStatusLine().getStatusCode();
        Assert.assertEquals(403, statusCode);
    }

}
