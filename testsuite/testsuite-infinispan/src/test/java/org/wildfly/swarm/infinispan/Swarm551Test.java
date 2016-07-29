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

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.junit.Test;
import org.wildfly.swarm.container.Container;
import org.wildfly.swarm.jaxrs.JAXRSArchive;
import org.wildfly.swarm.jaxrs.JAXRSFraction;
import org.wildfly.swarm.spi.api.Module;

/**
 * @author Heiko Braun
 */
public class Swarm551Test {

    @Test
    public void testCacheManagerAccess() throws Exception {

        Container container = new Container();
        container.fraction(new JAXRSFraction());
        container.fraction(InfinispanFraction.createDefaultFraction());

        container.start();

        JAXRSArchive archive = ShrinkWrap.create(JAXRSArchive.class, "testDeployment.war");
        archive.addResource(MyResource.class);

      /*  Module infinispan = archive.addModule("org.infinispan");
        infinispan.withExport(true);
        Module commons = archive.addModule("org.infinispan.commons");
        commons.withExport(true);
*/

        container.deploy(archive);

        String response = getUrlContents("http://localhost:8080");

        System.out.println(response);

        container.stop();
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
