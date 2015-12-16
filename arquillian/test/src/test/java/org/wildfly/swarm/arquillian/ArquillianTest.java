/*
 * Copyright 2015 Red Hat, Inc, and individual contributors.
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
package org.wildfly.swarm.arquillian;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.swarm.container.JARArchive;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(Arquillian.class)
public class ArquillianTest {
    @ArquillianResource
    URL url;

    @ArquillianResource
    URI uri;

    static final URI EXPECTED_URI;
    static final URL EXPECTED_URL;

    static {
        try {
            EXPECTED_URI = new URI("http://127.0.0.1:8080/");
            EXPECTED_URL = EXPECTED_URI.toURL();
        } catch (MalformedURLException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    @Deployment
    public static Archive createDeployment() {
        return ShrinkWrap.create(JARArchive.class)
                .addModule("progress")
                .addClass(ArquillianTest.class);
    }

    @Test
    @RunAsClient
    public void testOutside() throws Exception{
        // confirm the resource injectors work
        assertEquals(EXPECTED_URL, url);
        assertEquals(EXPECTED_URI, uri);
    }

    @Test
    public void testInside() throws Exception {
        // confirm we can load a custom module from a custom repo
        assertNotNull(ArquillianTest.class.getClassLoader().getResource("progress/bar.clj"));

        // confirm the resource injectors work
        assertEquals(EXPECTED_URL, url);
        assertEquals(EXPECTED_URI, uri);
    }
}
