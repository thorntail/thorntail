/**
 * Copyright 2015-2017 Red Hat, Inc, and individual contributors.
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
package org.wildfly.swarm.topology.consul;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.wildfly.swarm.Swarm;
import org.wildfly.swarm.arquillian.CreateSwarm;

import java.io.IOException;
import java.net.URI;
import java.util.Map;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author Michal Szynkiewicz, michal.l.szynkiewicz@gmail.com
 * <br>
 * Date: 4/11/17
 * Time: 2:46 PM
 */
abstract class AdvertisingTestBase {
    private static final ObjectMapper mapper = new ObjectMapper();

    protected static final String consulUrl = System.getProperty("consulUrl", "http://localhost:8500");
    protected static final String servicesUrl = String.format("%s/v1/catalog/services", consulUrl);

    @CreateSwarm
    public static Swarm newContainer() throws Exception {
        return new Swarm().fraction(new ConsulTopologyFraction(new URI(consulUrl).toURL()));
    }

    protected Map<?, ?> getDefinedServicesAsMap() throws IOException {
        HttpClientBuilder builder = HttpClientBuilder.create();
        CloseableHttpClient client = builder.build();

        HttpUriRequest request = new HttpGet(servicesUrl);
        CloseableHttpResponse response = client.execute(request);

        assertThat(response.getStatusLine().getStatusCode()).isEqualTo(200);
        String content = EntityUtils.toString(response.getEntity());
        return mapper.readValue(content, Map.class);
    }
}
