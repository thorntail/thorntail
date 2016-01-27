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

import org.wildfly.swarm.SwarmProperties;
import org.wildfly.swarm.container.Fraction;

/**
 * Consul topology-management fractoin.
 *
 * This fraction allows for the use of a cluster of Consul servers and agents
 * to manage topology information.
 *
 * @author John Hovell
 * @author Bob McWhirter
 */
public class ConsulTopologyFraction implements Fraction {

    /**
     * The default consul Agent URL (http://localhost:8500/)
     */
    private static final URL DEFAULT_URL;

    static {
        URL tmp = null;
        try {
            String consulUrl = System.getProperty(SwarmProperties.CONSUL_URL, "http://localhost:8500/");
            tmp = new URL(consulUrl);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        DEFAULT_URL = tmp;
    }

    private URL url = null;

    /**
     * Construct a default fraction using the default agent URL of http://localhost:8500/.
     */
    public ConsulTopologyFraction() {
        this.url = DEFAULT_URL;
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
        this.url = url;
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
        this.url = new URL(url);
        return this;
    }

    /**
     * Retrieve the configured agent URL.
     *
     * @return The agent URL.
     */
    public URL url() {
        return this.url;
    }


}
