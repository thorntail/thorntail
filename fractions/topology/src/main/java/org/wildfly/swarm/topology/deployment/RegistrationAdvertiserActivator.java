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
package org.wildfly.swarm.topology.deployment;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.enterprise.inject.Vetoed;

import org.jboss.msc.service.ServiceActivator;
import org.jboss.msc.service.ServiceActivatorContext;
import org.jboss.msc.service.ServiceRegistryException;
import org.jboss.msc.service.ServiceTarget;
import org.wildfly.swarm.topology.TopologyArchive;

/**
 * @author Bob McWhirter
 */
@Vetoed
public class RegistrationAdvertiserActivator implements ServiceActivator {

    @Override
    public void activate(ServiceActivatorContext context) throws ServiceRegistryException {

        ServiceTarget target = context.getServiceTarget();

        InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(TopologyArchive.REGISTRATION_CONF);

        if (in == null) {
            return;
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {

            String serviceName = null;

            while ((serviceName = reader.readLine()) != null) {
                serviceName = serviceName.trim();
                if (!serviceName.isEmpty()) {
                    RegistrationAdvertiser.install(target, serviceName, "http");
                    RegistrationAdvertiser.install(target, serviceName, "https");
                }
            }

        } catch (IOException e) {
            throw new ServiceRegistryException(e);
        }
    }


}
