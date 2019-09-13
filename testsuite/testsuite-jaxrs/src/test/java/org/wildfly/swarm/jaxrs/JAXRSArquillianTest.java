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
package org.wildfly.swarm.jaxrs;

import org.apache.http.client.fluent.Request;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author Bob McWhirter
 */
@RunWith(Arquillian.class)
public class JAXRSArquillianTest {
    @Deployment(testable = false)
    public static Archive createDeployment() throws Exception {
        return ShrinkWrap.create(JAXRSArchive.class, "myapp.war")
                .addClass(CustomJsonProvider.class)
                .addClass(MyResource.class)
                .setContextRoot("rest")
                .addAsResource("project-test-defaults-path.yml", "/project-defaults.yml")
                .addAllDependencies();
    }

    @Test
    @RunAsClient
    public void testSimple() throws IOException {
        String content = Request.Get("http://localhost:8080/rest").execute().returnContent().asString();
        assertThat(content).contains("Howdy at ");
    }
}
