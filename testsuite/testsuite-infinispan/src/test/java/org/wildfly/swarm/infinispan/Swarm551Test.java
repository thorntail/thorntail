package org.wildfly.swarm.infinispan;

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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.UUID;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.swarm.Swarm;
import org.wildfly.swarm.arquillian.CreateSwarm;
import org.wildfly.swarm.jaxrs.JAXRSArchive;
import org.wildfly.swarm.jaxrs.JAXRSFraction;

import static org.junit.Assert.assertNotNull;

/**
 * @author Heiko Braun
 */
@RunWith(Arquillian.class)
public class Swarm551Test {

    @Deployment(testable = false)
    public static Archive deployment() {
        JAXRSArchive archive = ShrinkWrap.create(JAXRSArchive.class, "testDeployment.war");
        archive.addResource(MyResource.class);
        return archive;
    }

    @CreateSwarm
    public static Swarm create() throws Exception {
        Swarm container = new Swarm();
        container.fraction(new JAXRSFraction());
        container.fraction(InfinispanFraction.createDefaultFraction());
        return container;
    }

    @RunAsClient
    @Test
    public void testAccess() {
        String response = getUrlContents("http://localhost:8080");
        response = response.trim();

        UUID uuid = UUID.fromString( response );
        assertNotNull( uuid );
    }

    static String getUrlContents(String theUrl) {
        StringBuilder content = new StringBuilder();

        try {
            URL url = new URL(theUrl);
            URLConnection urlConnection = url.openConnection();
            BufferedReader bufferedReader = new BufferedReader(
                    new InputStreamReader(urlConnection.getInputStream())
            );

            String line;

            while ((line = bufferedReader.readLine()) != null) {
                content.append(line + "\n");
            }
            bufferedReader.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return content.toString();
    }
}
