package org.wildfly.swarm.internal;

import org.wildfly.swarm.spi.api.OutboundSocketBinding;

/**
 * @author Bob McWhirter
 */
public class OutboundSocketBindingRequest {

    public OutboundSocketBindingRequest(String socketBindingGroup, OutboundSocketBinding binding) {
        this.socketBindingGroup = socketBindingGroup;
        this.binding = binding;
    }

    public String socketBindingGroup() {
        return this.socketBindingGroup;
    }

    public OutboundSocketBinding outboundSocketBinding() {
        return this.binding;
    }

    private final String socketBindingGroup;
    private final OutboundSocketBinding binding;
}
