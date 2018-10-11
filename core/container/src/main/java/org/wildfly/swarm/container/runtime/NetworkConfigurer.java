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
package org.wildfly.swarm.container.runtime;

import java.util.List;
import java.util.function.Consumer;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.wildfly.swarm.container.Interface;
import org.wildfly.swarm.spi.api.OutboundSocketBinding;
import org.wildfly.swarm.spi.api.SocketBinding;
import org.wildfly.swarm.spi.api.SocketBindingGroup;
import org.wildfly.swarm.spi.api.config.ConfigKey;
import org.wildfly.swarm.spi.api.config.ConfigView;
import org.wildfly.swarm.spi.api.config.SimpleKey;

/**
 * Created by bob on 7/5/17.
 */
@ApplicationScoped
public class NetworkConfigurer {

    private static final ConfigKey SWARM = ConfigKey.of("thorntail");

    private static ConfigKey ROOT = SWARM.append("network", "socket-binding-groups");

    public void configure() {
        for (Interface each : this.interfaces) {
            configure(each);
        }
        for (SocketBindingGroup each : this.socketBindingGroups) {
            configure(each);
        }
    }

    protected void configure(Interface iface) {
        fixInterface(iface);
    }

    protected void fixInterface(Interface iface) {
        if (iface.getName().equals("public")) {
            String bind = (String) this.configView.valueOf(SWARM.append("bind", "address"));
            if (bind != null) {
                iface.setExpression(bind);
            }
        } else {
            String bind = (String) this.configView.valueOf(SWARM.append("bind", iface.getName(), "address"));
            if (bind != null) {
                iface.setExpression(bind);
            }
        }
    }

    protected void configure(SocketBindingGroup group) {
        fixGroup(group);
        fixSocketBindings(group);
        fixOutboundSocketBindings(group);
    }

    protected void fixGroup(SocketBindingGroup group) {
        ConfigKey key = ROOT.append(group.name());

        int offset = this.configView.resolve(SWARM.append("port", "offset")).as(Integer.class).withDefault(0).getValue();

        group.portOffset(offset);

        applyConfiguration(key.append("port-offset"), (portOffset) -> {
            group.portOffset(portOffset.toString());
        });
        applyConfiguration(key.append("default-interface"), (defaultInterface) -> {
            group.defaultInterface(defaultInterface.toString());
        });

    }

    protected void fixSocketBindings(SocketBindingGroup group) {
        ConfigKey key = ROOT.append(group.name()).append("socket-bindings");

        List<SimpleKey> names = this.configView.simpleSubkeys(key);

        names.stream()
                .map(e -> e.name())
                .map(name ->
                             group.socketBindings()
                                     .stream()
                                     .filter(e -> e.name().equals(name))
                                     .findFirst()
                                     .orElseGet(() -> {
                                         SocketBinding binding = new SocketBinding(name);
                                         group.socketBinding(binding);
                                         return binding;
                                     }))
                .forEach(e -> {
                    applyConfiguration(key, e);
                });
    }

    protected void fixOutboundSocketBindings(SocketBindingGroup group) {
        ConfigKey key = ROOT.append(group.name()).append("outbound-socket-bindings");

        List<SimpleKey> names = this.configView.simpleSubkeys(key);

        names.stream()
                .map(e -> e.name())
                .map(name ->
                             group.outboundSocketBindings()
                                     .stream()
                                     .filter(e -> e.name().equals(name))
                                     .findFirst()
                                     .orElseGet(() -> {
                                         OutboundSocketBinding binding = new OutboundSocketBinding(name);
                                         group.outboundSocketBinding(binding);
                                         return binding;
                                     }))
                .forEach(e -> {
                    applyConfiguration(key, e);
                });

    }

    protected void applyConfiguration(ConfigKey root, SocketBinding binding) {
        ConfigKey key = root.append(binding.name());

        applyConfiguration(key.append("port"), (port) -> {
            binding.port(port.toString());
        });

        applyConfiguration(key.append("multicast-port"), (port) -> {
            binding.multicastPort(port.toString());
        });

        applyConfiguration(key.append("multicast-address"), (addr) -> {
            binding.multicastAddress(addr.toString());
        });

        applyConfiguration(key.append("interface"), (iface) -> {
            binding.iface(iface.toString());
        });
    }

    protected void applyConfiguration(ConfigKey root, OutboundSocketBinding binding) {
        ConfigKey key = root.append(binding.name());

        applyConfiguration(key.append("remote-host"), (host) -> {
            binding.remoteHost(host.toString());
        });

        applyConfiguration(key.append("remote-port"), (port) -> {
            binding.remotePort(port.toString());
        });

    }

    protected void applyConfiguration(ConfigKey key, Consumer<Object> consumer) {
        Object value = this.configView.valueOf(key);
        if (value != null) {
            consumer.accept(value);
        }
    }

    @Inject
    private ConfigView configView;

    @Inject
    @Any
    private Instance<Interface> interfaces;

    @Inject
    @Any
    private Instance<SocketBindingGroup> socketBindingGroups;
}
