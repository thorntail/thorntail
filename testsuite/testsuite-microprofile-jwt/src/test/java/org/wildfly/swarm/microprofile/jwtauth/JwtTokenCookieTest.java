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

import static org.fest.assertions.Assertions.assertThat;
import static org.wildfly.swarm.microprofile.jwtauth.utils.TokenUtils.createToken;

import org.apache.http.client.fluent.Request;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.swarm.microprofile.jwtauth.roles.TestApplication;
import org.wildfly.swarm.undertow.WARArchive;

@RunWith(Arquillian.class)
public class JwtTokenCookieTest {
    @Deployment
    public static Archive<?> createDeployment() {
        return initDeployment().addAsResource("project-jwt-cookie.yml", "project-defaults.yml");
    }

    protected static WARArchive initDeployment() {
        WARArchive deployment = ShrinkWrap.create(WARArchive.class);
        deployment.addClass(DefaultGroupClaimResource.class);
        deployment.addClass(TestApplication.class);
        return deployment;
    }

    @RunAsClient
    @Test
    public void tokenInCookieHeader() throws Exception {
        String response = Request.Get("http://localhost:8080/mpjwt/default-group/secured")
                .setHeader("Cookie", "a=b;jwt=" + createToken("User"))
                .execute().returnContent().asString();
        assertThat(response).isEqualTo("User");
    }
}
