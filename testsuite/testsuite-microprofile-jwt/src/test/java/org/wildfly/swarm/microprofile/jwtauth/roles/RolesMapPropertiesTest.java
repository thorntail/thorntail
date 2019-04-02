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
package org.wildfly.swarm.microprofile.jwtauth.roles;

import static org.wildfly.swarm.microprofile.jwtauth.utils.TokenUtils.createToken;

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
import org.wildfly.swarm.undertow.WARArchive;

@RunWith(Arquillian.class)
public class RolesMapPropertiesTest {

    @Deployment
    public static Archive<?> createDeployment() throws Exception {
        WARArchive deployment = ShrinkWrap.create(WARArchive.class);
        deployment.addClass(RolesMapEndpoint.class);
        deployment.addClass(TestApplication.class);
        deployment.addAsResource("project-simple-config-roles-map.yml", "project-defaults.yml");
        deployment.addAsManifestResource(new ClassLoaderAsset("keys/public-key.pem"), "/MP-JWT-SIGNER");
        return deployment;
    }
    
    
    @RunAsClient
    @Test
    public void testRolesAllowed() throws Exception {
        String response = Request.Get("http://localhost:8080/mpjwt/rolesMap/echoer2")
            .setHeader("Authorization", "Bearer " + createToken("Echoer"))
            .execute().returnContent().asString();
        Assert.assertEquals(response, "Hello jdoe@example.com");
    }
    
    @RunAsClient
    @Test
    public void testRolesNotAllowed() throws Exception {
        Assert.assertEquals(403, 
            Request.Get("http://localhost:8080/mpjwt/rolesMap/echoer1")
                .setHeader("Authorization", "Bearer " + createToken("Echoer"))
                .execute().returnResponse().getStatusLine().getStatusCode()); 
    }

}
