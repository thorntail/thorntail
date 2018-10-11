package org.wildfly.swarm.container.runtime.usage;

import org.wildfly.swarm.container.Interface;
import org.wildfly.swarm.spi.api.SocketBinding;
import org.wildfly.swarm.spi.api.SocketBindingGroup;

/**
 * Created by bob on 9/7/17.
 */
public class NetworkVariableSupplier implements UsageVariableSupplier {

    public NetworkVariableSupplier(Iterable<Interface> interfaces, Iterable<SocketBindingGroup> socketBindings, UsageVariableSupplier delegate) {
        this.interfaces = interfaces;
        this.socketBindings = socketBindings;
        this.delegate = delegate;
    }

    @Override
    public Object valueOf(String name) throws Exception {
        // very special case
        if (name.equals("thorntail.public.url.base")) {
            return "http://" + valueOf("thorntail.public.host") + ":" + valueOf("thorntail.http.port") + "/";
        }

        String[] parts = name.split("\\.");
        if (parts.length > 0 && parts[0].equals("swarm")) {
            if (parts.length == 3) {
                for (Interface each : this.interfaces) {
                    if (parts[1].equals(each.getName())) {
                        if (parts[2].equals("host")) {
                            return each.getExpression();
                        }
                    }
                }
            }

            Object value = null;
            for (SocketBindingGroup each : this.socketBindings) {
                value = valueOf(each, name);
                if (value != null) {
                    break;
                }
            }

            if (value != null) {
                return value;
            }
        }

        return this.delegate.valueOf(name);
    }

    protected Object valueOf(SocketBindingGroup group, String name) throws Exception {
        String[] parts = name.split("\\.");

        String bindingName = null;
        String which = "port";

        if (parts.length == 3) {
            bindingName = parts[1];
        } else if (parts.length == 4) {
            bindingName = parts[1];
            which = parts[2];
        }

        int offset = Integer.parseInt(group.portOffsetExpression());

        for (SocketBinding socketBinding : group.socketBindings()) {
            if (socketBinding.name().equals(bindingName)) {
                if (which.equals("port")) {
                    int port = Integer.parseInt(socketBinding.portExpression());
                    return "" + (port + offset);
                } else if (which.equals("multicast-port")) {
                    int port = Integer.parseInt(socketBinding.multicastPortExpression());
                    return "" + (port + offset);
                } else if (which.equals("multicast-address")) {
                    String addr = socketBinding.multicastAddress();
                    return addr;
                } else if (which.equals("host")) {
                    return valueOf("thorntail." + socketBinding.iface() + ".host");
                }
            }
        }

        return null;
    }


    private final UsageVariableSupplier delegate;

    private final Iterable<SocketBindingGroup> socketBindings;

    private final Iterable<Interface> interfaces;
}
