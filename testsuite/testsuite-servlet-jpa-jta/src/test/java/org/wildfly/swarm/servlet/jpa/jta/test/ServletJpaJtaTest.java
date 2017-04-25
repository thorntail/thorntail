/**
 * Copyright 2017 Red Hat, Inc, and individual contributors.
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
package org.wildfly.swarm.servlet.jpa.jta.test;

import java.io.IOException;

import org.apache.http.client.fluent.Request;
import org.apache.http.message.BasicNameValuePair;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.swarm.arquillian.DefaultDeployment;

import static org.fest.assertions.Assertions.assertThat;

@RunWith(Arquillian.class)
@DefaultDeployment
public class ServletJpaJtaTest {
    private static int id = -1;

    @Test
    @RunAsClient
    @InSequence(1)
    public void empty() throws IOException {
        String result = Request.Get("http://localhost:8080/").execute().returnContent().asString().trim();
        assertThat(result).isEmpty();
    }

    @Test
    @RunAsClient
    @InSequence(2)
    public void create() throws IOException {
        String result = Request.Post("http://localhost:8080/").bodyForm(
                new BasicNameValuePair("title", "Title"),
                new BasicNameValuePair("author", "Author")
        ).execute().returnContent().asString().trim();

        assertThat(result).isNotEmpty();
        id = Integer.parseInt(result);
    }

    @Test
    @RunAsClient
    @InSequence(3)
    public void notEmpty() throws IOException {
        String result = Request.Get("http://localhost:8080/").execute().returnContent().asString().trim();
        assertThat(result).isNotEmpty().contains("1 Author: Title");
    }

    @Test
    @RunAsClient
    @InSequence(4)
    public void delete() throws IOException {
        String result = Request.Delete("http://localhost:8080/?id=" + id).execute().returnContent().asString().trim();
        assertThat(result).isEqualTo(id + " deleted");
    }

    @Test
    @RunAsClient
    @InSequence(5)
    public void emptyAgain() throws IOException {
        String result = Request.Get("http://localhost:8080/").execute().returnContent().asString().trim();
        assertThat(result).isEmpty();
    }
}
