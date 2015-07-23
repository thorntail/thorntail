package org.wildfly.swarm.clustering;

/**
 * @author Bob McWhirter
 */
public class SocketBindingProtocol extends Protocol {

    private final String socketBinding;

    public SocketBindingProtocol(String name, String socketBinding) {
        super(name);
        this.socketBinding = socketBinding;
    }

    public String socketBinding() {
        return this.socketBinding;
    }
}
