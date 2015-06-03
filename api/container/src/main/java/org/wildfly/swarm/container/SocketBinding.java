package org.wildfly.swarm.container;

/**
 * @author Bob McWhirter
 */
public class SocketBinding {

    private final String name;

    private final String portExpression;

    public SocketBinding(String name, String portExpression) {
        this.name = name;
        this.portExpression = portExpression;
    }

    public String name() {
        return this.name;
    }

    public String portExpression() {
        return this.portExpression;
    }


}
