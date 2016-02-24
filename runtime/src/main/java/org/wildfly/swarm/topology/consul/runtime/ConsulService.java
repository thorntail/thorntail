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
package org.wildfly.swarm.topology.consul.runtime;

import java.net.URL;

import com.orbitz.consul.Consul;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;

/**
 * MSC service providing the root Consul client.
 *
 * @author John Hovell
 * @author Bob McWhirter
 */
public class ConsulService implements Service<Consul> {

    public static final ServiceName SERVICE_NAME = ServiceName.of("swarm.topology.consul");

    private final URL url;

    private Consul consul;

    public ConsulService(URL url) {
        this.url = url;
    }

    @Override
    public void start(StartContext startContext) throws StartException {

        Consul.Builder builder = Consul.builder();


        // pool because of multiple threads.
        ResteasyClientBuilder clientBuilder = new ResteasyClientBuilder();
        clientBuilder = clientBuilder.connectionPoolSize(20);

        builder.withClientBuilder(clientBuilder);
        builder.withUrl(this.url);

        try {
            this.consul = builder.build();
        } catch (Exception e) {
            throw new StartException("Failed to connect consul at "+url, e);
        }
    }

    @Override
    public void stop(StopContext stopContext) {

    }

    @Override
    public Consul getValue() throws IllegalStateException, IllegalArgumentException {
        return this.consul;
    }
}
