package org.wildfly.swarm.jsp.test;

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

import org.apache.http.client.fluent.Request;
import org.junit.Test;

import java.io.IOException;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author Heiko Braun
 */
public class JstlIT {

    @Test
    public void jstlTransform() throws IOException {
        String result = Request.Get("http://localhost:8080/xslt.jsp").execute().returnContent().asString();
        System.out.println(result);
        assertThat(result).contains("<table");
        assertThat(result).contains("<i>Padam History</i>");
        assertThat(result).contains("<i>Great Mistry</i>");
    }
}
