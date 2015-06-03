package org.wildfly.swarm.container;

import java.util.ArrayList;
import java.util.List;


/**
 * @author Bob McWhirter
 */
public class SocketBindingGroup {


    private final String name;

    private final String defaultInterace;

    private final String portOffsetExpression;

    private List<SocketBinding> socketBindings = new ArrayList<>();

    public SocketBindingGroup(String name, String defaultInterface, String portOffsetExpression) {
        this.name = name;
        this.defaultInterace = defaultInterface;
        this.portOffsetExpression = portOffsetExpression;
    }

    public String name() {
        return this.name;
    }

    public String defaultInterface() {
        return this.defaultInterace;
    }

    public String portOffsetExpression() {
        return this.portOffsetExpression;
    }

    public SocketBindingGroup socketBinding(String name, String portExpression) {
        this.socketBindings.add(new SocketBinding(name, portExpression));
        return this;
    }

    public SocketBindingGroup socketBinding(String name, int port) {
        this.socketBindings.add(new SocketBinding(name, "" + port));
        return this;
    }

    public List<SocketBinding> socketBindings() {
        return this.socketBindings;
    }
}

