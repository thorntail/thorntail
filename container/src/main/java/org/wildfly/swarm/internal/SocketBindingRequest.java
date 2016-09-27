package org.wildfly.swarm.internal;

import org.wildfly.swarm.spi.api.SocketBinding;

/**
 * @author Bob McWhirter
 */
public class SocketBindingRequest {

    public SocketBindingRequest(String socketBindingGroup, SocketBinding binding) {
        this.socketBindingGroup = socketBindingGroup;
        this.binding = binding;
    }

    public String socketBindingGroup() {
        return this.socketBindingGroup;
    }

    public SocketBinding socketBinding() {
        return this.binding;
    }

    private final String socketBindingGroup;
    private final SocketBinding binding;
}
