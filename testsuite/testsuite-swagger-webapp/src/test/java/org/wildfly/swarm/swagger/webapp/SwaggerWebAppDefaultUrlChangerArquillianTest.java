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
package org.wildfly.swarm.swagger.webapp;

import static org.fest.assertions.Assertions.assertThat;

import java.net.HttpURLConnection;
import java.net.URL;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.swarm.Swarm;
import org.wildfly.swarm.arquillian.CreateSwarm;
import org.wildfly.swarm.cdi.CDIFraction;
import org.wildfly.swarm.spi.api.JARArchive;

/**
 * @author John Alstrom
 */
@RunWith(Arquillian.class)
public class SwaggerWebAppDefaultUrlChangerArquillianTest {

    @SuppressWarnings("rawtypes")
    @Deployment(testable = false)
    public static Archive createDeployment() {
        JARArchive deployment = ShrinkWrap.create(JARArchive.class);
        deployment.addAsResource("project-application-path.yml");
        return deployment;
    }

    @CreateSwarm
    public static Swarm newContainer() throws Exception {
        Swarm swarm = new Swarm();
        swarm.fraction(new CDIFraction());
        swarm.withProfile("application-path");
        return swarm;
    }

    @RunAsClient
    @Test
    public void testEndpoints() throws Exception {
        String newUrl = getUrl("http://127.0.0.1:8080/api/docs/");
        assertThat(newUrl).isEqualTo("http://127.0.0.1:8080/api/docs/index.html?url=/rest/swagger.json");
    }
    
    private static String getUrl(String theUrl) {
        StringBuilder content = new StringBuilder();

        try {
            URL url = new URL(theUrl);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setInstanceFollowRedirects(false);
            
            int responseCode = urlConnection.getResponseCode();
            if(responseCode == 302) {
                String newUrl = urlConnection.getHeaderField("Location");
                content.append(newUrl);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return content.toString();
    }

}
