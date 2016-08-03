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
package org.wildfly.swarm.management;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Named;

import org.wildfly.swarm.spi.api.Customizer;
import org.wildfly.swarm.spi.api.Pre;
import org.wildfly.swarm.spi.api.SocketBinding;
import org.wildfly.swarm.spi.api.SocketBindingGroup;
import org.wildfly.swarm.spi.api.SwarmProperties;

/**
 * @author Bob McWhirter
 */
@Pre
@ApplicationScoped
public class ManagementSocketBindingsCustomizer implements Customizer {

    @Inject
    @Named("standard-sockets")
    private SocketBindingGroup group;

    public void customize() {
        this.group.socketBinding(new SocketBinding("management-http")
                .port(SwarmProperties.propertyVar(ManagementProperties.HTTP_PORT, "9990")));
        this.group.socketBinding(new SocketBinding("management-https")
                .port(SwarmProperties.propertyVar(ManagementProperties.HTTPS_PORT, "9993")));
    }
}
