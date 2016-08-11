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
package org.wildfly.swarm.undertow.runtime;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Named;

import org.wildfly.swarm.spi.api.Customizer;
import org.wildfly.swarm.spi.api.SocketBinding;
import org.wildfly.swarm.spi.api.SocketBindingGroup;
import org.wildfly.swarm.spi.api.SwarmProperties;
import org.wildfly.swarm.spi.runtime.annotations.Pre;
import org.wildfly.swarm.undertow.UndertowFraction;

/**
 * @author Bob McWhirter
 */
@Pre
@ApplicationScoped
public class AJPCustomizer implements Customizer {

    @Inject
    @Any
    private Instance<UndertowFraction> undertowInstance;

    @Inject
    @Named("standard-sockets")
    private SocketBindingGroup group;

    public AJPCustomizer() {
    }

    public SocketBinding ajpSocketBinding() {
        return null;
    }

    public void customize() {
        UndertowFraction fraction = undertowInstance.get();
        if (fraction.isEnableAJP()) {
            fraction.subresources().servers().stream()
                    .filter(server -> server.subresources().ajpListeners().isEmpty())
                    .forEach(server -> server.ajpListener("ajp", listener -> listener.socketBinding("ajp")));

            this.group.socketBinding(new SocketBinding("ajp")
                                             .port(SwarmProperties.propertyVar(SwarmProperties.AJP_PORT, "8009")));
        }
    }

}
