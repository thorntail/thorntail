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

import org.apache.commons.io.IOUtils;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.swarm.arquillian.DefaultDeployment;

import java.net.URI;

@RunWith(Arquillian.class)
@DefaultDeployment
public class OpenApiTest {

    private static String getUrlContent(String url) throws Exception {
        return IOUtils.toString(new URI(url), "UTF-8");
    }

    @Test
    public void testOpenApi() throws Exception {
        String content = getUrlContent("http://localhost:8080/openapi");
        Assert.assertNotNull(content);
    }

}
