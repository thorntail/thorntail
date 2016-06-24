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
package org.wildfly.swarm.jaxrs;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ArchivePath;
import org.jboss.shrinkwrap.api.Node;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.swarm.ContainerFactory;
import org.wildfly.swarm.container.Container;
import org.wildfly.swarm.spi.api.ArtifactLookup;

import static org.fest.assertions.Assertions.*;

/**
 * @author Bob McWhirter
 */
@RunWith(Arquillian.class)
public class ArqJAXRSExceptionMapperTest {

    @Deployment(testable = false)
    public static Archive createDeployment() throws Exception {
        JAXRSArchive deployment = ShrinkWrap.create(JAXRSArchive.class, "myapp.war");
        deployment.addClass(JsonProcessingExceptionMapper.class);
        deployment.addClass(CustomExceptionMapper.class);
        deployment.addClass(CustomException.class);
        deployment.addResource(JsonParsingExceptionResource.class);
        deployment.addResource(CustomExceptionResource.class);
        deployment.addAllDependencies();
        return deployment;
    }

    @Test
    @RunAsClient
    public void testCustom() throws Exception {
        String content = getUrlContents("http://localhost:8080/custom/throw");
        assertThat( content ).contains( "mapped custom: Custom exception" );
    }

    @Test
    @RunAsClient
    public void testJson() throws Exception {
        String content = getUrlContents("http://localhost:8080/json/throw");
        assertThat( content ).contains( "mapped json: that didn't work" );
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
