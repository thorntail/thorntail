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

import com.orbitz.consul.AgentClient;
import com.orbitz.consul.Consul;
import org.jboss.msc.inject.Injector;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;

/**
 * MSC Service providing a Consul AgentClient.
 *
 * @author John Hovell
 * @author Bob McWhirter
 */
public class AgentClientService implements Service<AgentClient> {

    public static final ServiceName SERVICE_NAME = ConsulService.SERVICE_NAME.append("agent-client");

    private InjectedValue<Consul> consulInjector = new InjectedValue<>();

    private AgentClient client;

    public Injector<Consul> getConsulInjector() {
        return this.consulInjector;
    }

    @Override
    public void start(StartContext startContext) throws StartException {
        this.client = this.consulInjector.getValue().agentClient();
    }

    @Override
    public void stop(StopContext stopContext) {
        this.client = null;
    }

    @Override
    public AgentClient getValue() throws IllegalStateException, IllegalArgumentException {
        return this.client;
    }
}
