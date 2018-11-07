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
package org.wildfly.swarm.jsonp;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonWriter;
import org.apache.http.entity.StringEntity;
import org.apache.http.client.fluent.Request;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.swarm.undertow.WARArchive;

import static org.fest.assertions.Assertions.assertThat;

@RunWith(Arquillian.class)
public class JavaxJsonpTest {

    @Deployment
    public static Archive createDeployment() throws Exception {
        WARArchive deployment = ShrinkWrap.create(WARArchive.class);
        deployment.addClass(JsonpServlet.class);
        return deployment;
    }


    @Test
    @RunAsClient
    public void testJavaxJsonp() throws IOException {
        JsonObject jsonOut = Json.createObjectBuilder()
                .add("greeting", "hi")
                .build();
        StringWriter stringWriter = new StringWriter(); 
        JsonWriter jsonWriter = Json.createWriter(stringWriter);
        jsonWriter.writeObject(jsonOut);

        String response = Request.Post("http://localhost:8080/jsonp").body(new StringEntity(stringWriter.toString()))
            .execute().returnContent().asString().trim();
        JsonReader jsonReader = Json.createReader(new StringReader(response));
        JsonObject jsonIn = jsonReader.readObject();

        assertThat(jsonIn.getString("greeting")).isEqualTo("hi");
        assertThat(jsonIn.getString("fromServlet")).isEqualTo("true");
    }
}
