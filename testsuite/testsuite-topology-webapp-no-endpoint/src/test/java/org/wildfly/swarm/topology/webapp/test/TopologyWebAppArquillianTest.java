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
package org.wildfly.swarm.topology.webapp.test;

import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.ServiceRegistry;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.swarm.arquillian.DefaultDeployment;

import static org.junit.Assert.assertNotNull;

/**
 * @author Lance Ball
 * @author Ken Finnigan
 */
@RunWith(Arquillian.class)
@DefaultDeployment(type = DefaultDeployment.Type.WAR)
public class TopologyWebAppArquillianTest {

    /*
    @CreateSwarm
    public static Swarm newContainer() throws Exception {
        TopologyWebAppFraction topology = new TopologyWebAppFraction();
        topology.proxyService("myService", "/my-proxy");

        return new Swarm()
                .fraction(topology)
                .fraction(new JGroupsTopologyFraction());
    }
    */

    @ArquillianResource
    private ServiceRegistry registry;

    @Test
    public void testTopologyProxyHandlerPresence() throws Exception {
        ServiceController<?> proxyService = registry.getService(ServiceName.parse("swarm.topology.proxy"));
        assertNotNull("TopologyProxyService is not available in service registry", proxyService);

        ServiceController<?> proxyHandler = registry
                .getService(ServiceName.parse("org.wildfly.extension.undertow.handler.myService-proxy-handler"));
        assertNotNull("`myService` Undertow handler is not available in service registry", proxyHandler);
    }
}
