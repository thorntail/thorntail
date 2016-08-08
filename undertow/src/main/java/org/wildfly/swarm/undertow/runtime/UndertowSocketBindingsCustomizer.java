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
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.wildfly.swarm.spi.api.Customizer;
import org.wildfly.swarm.spi.api.SocketBinding;
import org.wildfly.swarm.spi.api.SocketBindingGroup;
import org.wildfly.swarm.spi.api.annotations.ConfigurationValue;
import org.wildfly.swarm.spi.runtime.annotations.Post;
import org.wildfly.swarm.spi.runtime.annotations.Pre;
import org.wildfly.swarm.undertow.UndertowFraction;

/**
 * @author Bob McWhirter
 */
@Pre
@Singleton
public class UndertowSocketBindingsCustomizer implements Customizer {

    @Inject
    @Named("standard-sockets")
    private SocketBindingGroup group;

    @Inject
    @Any
    UndertowFraction fraction;

    @Inject
    @ConfigurationValue("swarm.http.port")
    Integer httpPort;

    @Inject
    @ConfigurationValue("swarm.https.port")
    Integer httpsPort;

    public void customize() {

        if (this.httpPort == null) {
            this.httpPort = this.fraction.httpPort();
        }

        if (this.httpsPort == null) {
            this.httpsPort = this.fraction.httpsPort();
        }

        this.group.socketBinding(new SocketBinding("http")
                .port(this.httpPort));
        this.group.socketBinding(new SocketBinding("https")
                .port(this.httpsPort));
    }

}
