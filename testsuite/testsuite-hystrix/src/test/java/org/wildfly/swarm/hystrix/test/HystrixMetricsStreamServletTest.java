/**
 * Copyright 2017 Red Hat, Inc, and individual contributors.
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
package org.wildfly.swarm.hystrix.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.swarm.arquillian.DefaultDeployment;
import org.wildfly.swarm.netflix.hystrix.HystrixProperties;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;

/**
 *
 * @author Martin Kouba
 */
@RunWith(Arquillian.class)
@DefaultDeployment
public class HystrixMetricsStreamServletTest {

    @Test
    @RunAsClient
    public void testHystrixStream(@ArquillianResource URL url) throws FailingHttpStatusCodeException, MalformedURLException, IOException {
        HttpGet get = new HttpGet(url + HystrixProperties.DEFAULT_STREAM_PATH);
        try (CloseableHttpClient httpclient = HttpClients.createDefault(); CloseableHttpResponse response = httpclient.execute(get)) {
            assertEquals(200, response.getStatusLine().getStatusCode());
            assertTrue(response.getFirstHeader("Content-Type").getValue().startsWith("text/event-stream"));
            // Ignore the response body
            get.abort();
        }
    }

}
