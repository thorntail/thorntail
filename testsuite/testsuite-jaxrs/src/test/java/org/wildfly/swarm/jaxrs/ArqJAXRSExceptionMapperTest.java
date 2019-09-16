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

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author Bob McWhirter
 */
@RunWith(Arquillian.class)
public class ArqJAXRSExceptionMapperTest {
    @Deployment(testable = false)
    public static Archive createDeployment() throws Exception {
        return ShrinkWrap.create(JAXRSArchive.class, "myapp.war")
                .addClasses(CustomException.class, CustomExceptionMapper.class, CustomExceptionResource.class,
                        JsonParsingExceptionResource.class, JsonProcessingExceptionMapper.class)
                .addAsResource("project-test-defaults-path.yml", "/project-defaults.yml")
                .addAllDependencies();
    }

    @Test
    @RunAsClient
    public void testCustom() throws Exception {
        String content = Request.Get("http://localhost:8080/custom/throw").execute().returnContent().asString();
        assertThat(content).contains("mapped custom: Custom exception");
    }

    @Test
    @RunAsClient
    public void testJson() throws Exception {
        String content = Request.Get("http://localhost:8080/json/throw").execute().returnContent().asString();
        assertThat(content).contains("mapped json: that didn't work");
    }
}
