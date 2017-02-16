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
package org.wildfly.swarm.container.runtime.cli;

import java.util.Arrays;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.jboss.msc.service.ServiceActivator;
import org.jboss.msc.service.ServiceActivatorContext;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.ServiceRegistryException;
import org.jboss.msc.service.ValueService;
import org.jboss.msc.value.ImmediateValue;
import org.wildfly.swarm.internal.SwarmMessages;

/**
 * @author Bob McWhirter
 */
@ApplicationScoped
public class CommandLineArgsServiceActivator implements ServiceActivator {

    @Inject
    @CommandLineArgs
    String[] args;

    @Override
    public void activate(ServiceActivatorContext context) throws ServiceRegistryException {
        SwarmMessages.MESSAGES.argsInstalled(Arrays.asList(this.args));
        context.getServiceTarget().addService(ServiceName.of("wildfly", "swarm", "main-args"), new ValueService<>(new ImmediateValue<>(this.args)))
                .install();
    }
}
