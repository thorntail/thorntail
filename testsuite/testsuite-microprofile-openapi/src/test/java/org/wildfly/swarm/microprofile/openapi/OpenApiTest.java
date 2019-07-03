/**
 * Copyright 2018 Red Hat, Inc, and individual contributors.
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

package org.wildfly.swarm.microprofile.openapi;

import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Request;
import org.apache.http.util.EntityUtils;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.swarm.arquillian.DefaultDeployment;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(Arquillian.class)
@DefaultDeployment
public class OpenApiTest {
    @Test
    @RunAsClient
    public void testOpenApi() throws Exception {
        HttpResponse response = Request.Get("http://localhost:8080/openapi").execute().returnResponse();
        assertTrue(response.getFirstHeader("Content-Type").getValue().contains("application/yaml"));
        String content = EntityUtils.toString(response.getEntity());
        assertNotNull(content);
        assertTrue(content.contains("/api/foo/hello"));
    }

    @Test
    @RunAsClient
    public void acceptYaml() throws Exception {
        HttpResponse response = Request.Get("http://localhost:8080/openapi")
                .addHeader("Accept", "application/yaml")
                .execute()
                .returnResponse();
        assertTrue(response.getFirstHeader("Content-Type").getValue().contains("application/yaml"));
    }

    @Test
    @RunAsClient
    public void acceptJson() throws Exception {
        HttpResponse response = Request.Get("http://localhost:8080/openapi")
                .addHeader("Accept", "application/json")
                .execute()
                .returnResponse();
        assertTrue(response.getFirstHeader("Content-Type").getValue().contains("application/json"));
    }

    @Test
    @RunAsClient
    public void acceptNonsense() throws Exception {
        HttpResponse response = Request.Get("http://localhost:8080/openapi")
                .addHeader("Accept", "foo/bar")
                .execute()
                .returnResponse();
        assertTrue(response.getFirstHeader("Content-Type").getValue().contains("application/yaml"));
    }

    @Test
    @RunAsClient
    public void formatYaml() throws Exception {
        HttpResponse response = Request.Get("http://localhost:8080/openapi?format=YAML").execute().returnResponse();
        assertTrue(response.getFirstHeader("Content-Type").getValue().contains("application/yaml"));
    }

    @Test
    @RunAsClient
    public void formatJson() throws Exception {
        HttpResponse response = Request.Get("http://localhost:8080/openapi?format=JSON").execute().returnResponse();
        assertTrue(response.getFirstHeader("Content-Type").getValue().contains("application/json"));
    }

    @Test
    @RunAsClient
    public void formatJsonLowercase() throws Exception {
        HttpResponse response = Request.Get("http://localhost:8080/openapi?format=json").execute().returnResponse();
        assertTrue(response.getFirstHeader("Content-Type").getValue().contains("application/json"));
    }

    @Test
    @RunAsClient
    public void formatJsonLegacy() throws Exception {
        HttpResponse response = Request.Get("http://localhost:8080/openapi?format=application/json").execute().returnResponse();
        assertTrue(response.getFirstHeader("Content-Type").getValue().contains("application/json"));
    }

    @Test
    @RunAsClient
    public void formatNonsense() throws Exception {
        HttpResponse response = Request.Get("http://localhost:8080/openapi?format=foo-bar").execute().returnResponse();
        assertTrue(response.getFirstHeader("Content-Type").getValue().contains("application/yaml"));
    }
}
