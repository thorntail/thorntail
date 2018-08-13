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
package org.wildfly.swarm.topology.consul;

import java.net.MalformedURLException;
import java.net.URL;

import org.wildfly.swarm.config.runtime.AttributeDocumentation;
import org.wildfly.swarm.spi.api.Defaultable;
import org.wildfly.swarm.spi.api.Fraction;
import org.wildfly.swarm.spi.api.annotations.Configurable;

/**
 * Consul topology-management fraction.
 *
 * This fraction allows for the use of a cluster of Consul servers and agents
 * to manage topology information.
 *
 * @author John Hovell
 * @author Bob McWhirter
 */
@Configurable("swarm.topology.consul")
public class ConsulTopologyFraction implements Fraction<ConsulTopologyFraction> {

    public ConsulTopologyFraction() {
        this(DEFAULT_URL);
    }

    /**
     * Construct with an agent URL
     *
     * @param url Agent URL
     */
    public ConsulTopologyFraction(URL url) {
        url(url);
    }

    /**
     * Construct with an agent URL
     *
     * @param url Agent URL
     */
    public ConsulTopologyFraction(String url) throws MalformedURLException {
        url(url);
    }

    /**
     * Set the agent URL
     *
     * @param url The agent URL.
     * @return this fraction.
     */
    public ConsulTopologyFraction url(URL url) {
        this.url.set(url);
        return this;
    }

    /**
     * Set the agent URL
     *
     * @param url The agent URL.
     * @return this fraction
     * @throws MalformedURLException if an error occurs parsing the URL.
     */
    public ConsulTopologyFraction url(String url) throws MalformedURLException {
        this.url.set(new URL(url));
        return this;
    }

    /**
     * Retrieve the configured agent URL.
     *
     * @return The agent URL.
     */
    public URL url() {
        return this.url.get();
    }

    public Long ttl() {
        return this.ttl.get();
    }

    /**
     * The default consul Agent URL (http://localhost:8500/)
     */
    private static final URL DEFAULT_URL;

    static {
        URL tmp = null;
        try {
            tmp = new URL("http://localhost:8500");
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        DEFAULT_URL = tmp;
    }

    @AttributeDocumentation("URL of the Consul server")
    private Defaultable<URL> url = Defaultable.url(DEFAULT_URL);

    @AttributeDocumentation("TTL for the consul health check for each service. Default 3s")
    private Defaultable<Long> ttl = Defaultable.longInteger(3);

}
