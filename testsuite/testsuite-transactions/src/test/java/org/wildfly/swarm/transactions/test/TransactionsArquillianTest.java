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
package org.wildfly.swarm.transactions.test;

import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.as.network.SocketBinding;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.ServiceRegistry;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.swarm.arquillian.DefaultDeployment;
import org.wildfly.swarm.spi.api.annotations.DeploymentModule;

import static org.junit.Assert.assertEquals;

/**
 * @author Bob McWhirter
 */
@RunWith(Arquillian.class)
@DefaultDeployment(type = DefaultDeployment.Type.JAR)
@DeploymentModule(name = "org.jboss.as.network")
public class TransactionsArquillianTest {

    @ArquillianResource
    ServiceRegistry registry;

    @Test
    public void testPorts() throws Exception {
        ServiceController<SocketBinding> statusManagerService = (ServiceController<SocketBinding>) registry.getService(ServiceName.parse("org.wildfly.network.socket-binding.txn-status-manager"));
        SocketBinding statusManager = statusManagerService.getValue();
        assertEquals(9876, statusManager.getAbsolutePort());

        ServiceController<SocketBinding> recoveryService = (ServiceController<SocketBinding>) registry.getService(ServiceName.parse("org.wildfly.network.socket-binding.txn-recovery-environment"));
        SocketBinding recovery = recoveryService.getValue();
        assertEquals(4567, recovery.getAbsolutePort());

    }

}
