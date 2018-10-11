/**
 * Copyright 2015-2017 Red Hat, Inc, and individual contributors.
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
package org.wildfly.swarm.management.runtime;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.wildfly.swarm.config.management.HTTPInterfaceManagementInterface;
import org.wildfly.swarm.config.runtime.AttributeDocumentation;
import org.wildfly.swarm.management.ManagementFraction;
import org.wildfly.swarm.spi.api.Customizer;
import org.wildfly.swarm.spi.api.Defaultable;
import org.wildfly.swarm.spi.api.SocketBinding;
import org.wildfly.swarm.spi.api.SocketBindingGroup;
import org.wildfly.swarm.spi.api.annotations.Configurable;
import org.wildfly.swarm.spi.runtime.annotations.Pre;

import static org.wildfly.swarm.spi.api.Defaultable.string;

/**
 * @author Bob McWhirter
 */
@Pre
@ApplicationScoped
public class ManagementSocketBindingsCustomizer implements Customizer {

    @Inject
    @Named("standard-sockets")
    private SocketBindingGroup group;

    @Inject
    ManagementFraction fraction;

    public void customize() {
        this.group.socketBinding(
                new SocketBinding("management-http")
                        .iface(iface.get())
                        .port(fraction.httpPort()));
        this.group.socketBinding(new SocketBinding("management-https")
                .port(fraction.httpsPort()));

        if (fraction.isHttpDisable()) {
            fraction.httpInterfaceManagementInterface((HTTPInterfaceManagementInterface<?>) null);
        }
    }

    @AttributeDocumentation("Interface to bind for the management ports")
    @Configurable("thorntail.management.bind.interface")
    private Defaultable<String> iface = string("management");
}
