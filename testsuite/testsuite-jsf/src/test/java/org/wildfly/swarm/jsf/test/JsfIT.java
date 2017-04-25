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
package org.wildfly.swarm.jsf.test;

import java.io.IOException;

import org.apache.http.client.fluent.Request;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

/**
 * This can't be a simple Arquillian test because for the WAR to fully build, the Maven WAR plugin needs to run.
 */
public class JsfIT {
    @Test
    public void jsf() throws IOException, InterruptedException {
        String result = Request.Get("http://localhost:8080/index.jsf").execute().returnContent().asString();
        assertThat(result).contains("Hello from JSF");
    }
}
