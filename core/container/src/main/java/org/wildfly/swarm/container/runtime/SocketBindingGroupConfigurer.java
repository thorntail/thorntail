package org.wildfly.swarm.container.runtime;

import java.util.List;
import java.util.function.Consumer;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

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
public class SocketBindingGroupConfigurer {

    private static ConfigKey ROOT = ConfigKey.of("swarm", "network", "socket-binding-groups");

    public void configure() {
        for (SocketBindingGroup each : this.socketBindingGroups) {
            configure(each);
        }
    }

    protected void configure(SocketBindingGroup group) {
        ConfigKey key = ROOT.append(group.name());

        fixSocketBindings(group);
        fixOutboundSocketBindings(group);
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
                                     .orElseGet(() -> new SocketBinding(name)))
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
                                     .orElseGet(() -> new OutboundSocketBinding(name)))
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
    private Instance<SocketBindingGroup> socketBindingGroups;
}
