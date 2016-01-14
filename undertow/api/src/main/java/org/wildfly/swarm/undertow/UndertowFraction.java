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
package org.wildfly.swarm.undertow;

import org.wildfly.swarm.SwarmProperties;
import org.wildfly.swarm.config.Undertow;
import org.wildfly.swarm.config.undertow.BufferCache;
import org.wildfly.swarm.config.undertow.HandlerConfiguration;
import org.wildfly.swarm.config.undertow.Server;
import org.wildfly.swarm.config.undertow.ServletContainer;
import org.wildfly.swarm.config.undertow.server.HTTPListener;
import org.wildfly.swarm.config.undertow.server.Host;
import org.wildfly.swarm.config.undertow.servlet_container.JSPSetting;
import org.wildfly.swarm.config.undertow.servlet_container.WebsocketsSetting;
import org.wildfly.swarm.container.Container;
import org.wildfly.swarm.container.Fraction;
import org.wildfly.swarm.container.SocketBinding;

/**
 * @author Bob McWhirter
 */
public class UndertowFraction extends Undertow<UndertowFraction> implements Fraction {

    public UndertowFraction() {
    }

    public static UndertowFraction createDefaultFraction() {
        UndertowFraction fraction = new UndertowFraction();

        fraction.server(
                new Server("default-server")
                        .httpListener(new HTTPListener("default")
                                .socketBinding("http"))
                        .host(new Host("default-host")))
                .bufferCache(new BufferCache("default"))
                .servletContainer(new ServletContainer("default")
                        .websocketsSetting(new WebsocketsSetting())
                        .jspSetting(new JSPSetting()))
                .handlerConfiguration(new HandlerConfiguration());

        return fraction;
    }

    @Override
    public void initialize(Container.InitContext initContext) {
        initContext.socketBinding(
                new SocketBinding("http")
                        .port(SwarmProperties.propertyVar(UndertowProperties.HTTP_PORT, "8080")));
        initContext.socketBinding(
                new SocketBinding("https")
                        .port(SwarmProperties.propertyVar(UndertowProperties.HTTPS_PORT, "8443")));
    }
}
