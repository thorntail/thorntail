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
package org.wildfly.swarm.ee.security;

import static org.fest.assertions.Assertions.assertThat;

import java.io.IOException;

import org.apache.http.client.fluent.Request;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.swarm.undertow.WARArchive;

@RunWith(Arquillian.class)
public class EESecurityTest {


    @Deployment
    public static Archive createDeployment() throws Exception {
        WARArchive deployment = ShrinkWrap.create(WARArchive.class);
        deployment.addClass(EESecurityServlet.class);
        deployment.addClass(SimpleAuthenticationMechanism.class);
        deployment.addClass(SimpleIdentityStore.class);
        deployment.addAsWebInfResource("jboss-web.xml");
        return deployment;
    }
    
    @Test
    @RunAsClient
    public void testCustomAuthenticationMechanism() throws IOException {
        String response1 = Request.Get("http://localhost:8080/security?name=thorntail1&password=secret1")
                .execute().returnContent().asString().trim();
        assertThat(response1).isEqualTo("thorntail1, role1:true, role2:false");
        String response2 = Request.Get("http://localhost:8080/security?name=thorntail2&password=secret2")
                .execute().returnContent().asString().trim();
        assertThat(response2).isEqualTo("thorntail2, role1:false, role2:true");
    }
}
