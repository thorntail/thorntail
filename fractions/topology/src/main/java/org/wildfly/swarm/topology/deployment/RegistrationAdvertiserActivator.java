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
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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


        forEachLine(TopologyArchive.REGISTRATION_CONF, registrationLine -> {
            int separatorIndex = registrationLine.indexOf(TopologyArchive.SERVICE_TAG_SEPARATOR);
            String serviceName = registrationLine;
            List<String> tags = Collections.emptyList();
            if (separatorIndex > 0) {
                serviceName = registrationLine.substring(0, separatorIndex);
                tags = getTags(registrationLine.substring(separatorIndex + 1));
            }
            RegistrationAdvertiser.install(target, serviceName, "http", tags);
            RegistrationAdvertiser.install(target, serviceName, "https", tags);
        });
    }

    private List<String> getTags(String tagsAsString) {
        return Stream.of(tagsAsString.split(TopologyArchive.TAG_SEPARATOR))
                .map(String::trim)
                .collect(Collectors.toList());
    }

    private void forEachLine(String resourceName, Consumer<String> consumer) {
        InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourceName);

        if (in == null) {
            return;
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
            String line;
            while ((line = reader.readLine()) != null) {
                consumer.accept(line.trim());
            }
        } catch (IOException e) {
            throw new ServiceRegistryException(e);
        }
    }


}
