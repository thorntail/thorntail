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
package org.wildfly.swarm.jgroups.runtime;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.wildfly.swarm.container.runtime.config.DefaultSocketBindingGroupProducer;
import org.wildfly.swarm.jgroups.JGroupsFraction;
import org.wildfly.swarm.spi.api.Customizer;
import org.wildfly.swarm.spi.api.SocketBinding;
import org.wildfly.swarm.spi.api.SocketBindingGroup;
import org.wildfly.swarm.spi.runtime.annotations.Pre;

/**
 * @author Bob McWhirter
 */
@Pre
@ApplicationScoped
public class JGroupsSocketBindingCustomizer implements Customizer {

    @Inject
    @Named(DefaultSocketBindingGroupProducer.STANDARD_SOCKETS)
    private SocketBindingGroup group;

    @Inject
    private JGroupsFraction fraction;

    @Override
    public void customize() {
        this.group.socketBinding(
                new SocketBinding("jgroups-udp")
                        .port(55200)
                        .multicastAddress(this.fraction.defaultMulticastAddress())
                        .multicastPort(45688));

        this.group.socketBinding(
                new SocketBinding("jgroups-udp-fd")
                        .port(54200));

        this.group.socketBinding(
                new SocketBinding("jgroups-mping")
                        .port(0)
                        .multicastAddress(this.fraction.defaultMulticastAddress())
                        .multicastPort(45700));

        this.group.socketBinding(
                new SocketBinding("jgroups-tcp")
                        .port(7600));

        this.group.socketBinding(
                new SocketBinding("jgroups-tcp-fd")
                        .port(57600));

    }
}
