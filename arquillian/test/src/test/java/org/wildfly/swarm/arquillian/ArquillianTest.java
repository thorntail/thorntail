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
package org.wildfly.swarm.arquillian;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.modules.ModuleClassLoader;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.swarm.spi.api.JARArchive;


@RunWith(Arquillian.class)
public class ArquillianTest {

    @Deployment
    public static Archive<?> createDeployment() {
        JARArchive archive = ShrinkWrap.create(JARArchive.class)
                .addClass(ArquillianTest.class);

        archive.addModule("progress");
        return archive;
    }

    @Test
    @RunAsClient
    public void testOutside() throws Exception {
        // confirm the resource injectors work
        Assert.assertEquals(EXPECTED_URL, url);
        Assert.assertEquals(EXPECTED_URI, uri);
    }

    @Test
    public void testInside() throws Exception {

        // confirm we can load a custom module from a custom repo
        ClassLoader classLoader = ArquillianTest.class.getClassLoader();
        Assert.assertTrue(classLoader instanceof ModuleClassLoader);
        Assert.assertNotNull(classLoader.getResource("progress/bar.clj"));

        // confirm the resource injectors work
        Assert.assertEquals(EXPECTED_URL, url);
        Assert.assertEquals(EXPECTED_URI, uri);
    }

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

    @ArquillianResource
    URL url;

    @ArquillianResource
    URI uri;
}
