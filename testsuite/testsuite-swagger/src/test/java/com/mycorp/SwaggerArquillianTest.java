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
package com.mycorp;

import org.apache.http.client.fluent.Request;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.swarm.arquillian.DefaultDeployment;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author Lance Ball
 */
@RunWith(Arquillian.class)
@DefaultDeployment
public class SwaggerArquillianTest {

    @RunAsClient
    @Test
    public void testEndpoints() throws Exception {
        String content = Request.Get("http://127.0.0.1:8080/swagger.json").execute().returnContent().asString();
        assertThat(content).contains("\"tags\":[{\"name\":\"theapp\"}]").contains("\"summary\":\"Say howdy\"");

        content = Request.Get("http://127.0.0.1:8080/swagger.yaml").execute().returnContent().asString();
        assertThat(content).contains("name: \"theapp\"").contains("summary: \"Say howdy\"");
    }

}
