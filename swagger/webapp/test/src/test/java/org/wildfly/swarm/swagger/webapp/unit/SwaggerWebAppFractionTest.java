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
package org.wildfly.swarm.swagger.webapp.unit;

import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.Node;
import org.junit.Test;
import org.wildfly.swarm.swagger.webapp.SwaggerWebAppFraction;

import static org.fest.assertions.Assertions.assertThat;


/**
 * @author Lance Ball
 */
public class SwaggerWebAppFractionTest {

    @Test
    public void testAddWebContentFromGAV() {
        SwaggerWebAppFraction fraction = new SwaggerWebAppFraction();
        fraction.addWebContent("org.wildfly.swarm:swagger-webapp-ui:war:" + SwaggerWebAppFraction.VERSION);
        assertArchive(fraction);
    }

    @Test
    public void testAddWebContentFromJar() {
        SwaggerWebAppFraction fraction = new SwaggerWebAppFraction();
        fraction.addWebContent("./test.jar");
        assertArchive(fraction);
    }

    @Test
    public void testAddWebContentFromDirectory() {
        SwaggerWebAppFraction fraction = new SwaggerWebAppFraction();
        fraction.addWebContent("./sut");
        Archive<?> archive = assertArchive(fraction);
        // make sure nested files are where they should be
        Node node = archive.get("/js/test.js");
        assertThat(node).isNotNull();
        node = archive.get("/js/lib/some-lib.js");
        assertThat(node).isNotNull();
    }

    private Archive<?> assertArchive(SwaggerWebAppFraction fraction) {
        Archive<?> archive = fraction.getWebContent();
        assertThat(archive).isNotNull();
        Node node = archive.get("/index.html");
        assertThat(node).isNotNull();
        return archive;
    }
}
