package org.wildfly.swarm.container;

/**
 * @author Bob McWhirter
 */
public class SocketBinding {

    private final String name;

    private String portExpression;
    private String multicastAddress;
    private String multicastPortExpression;

    public SocketBinding(String name) {
        this.name = name;
    }

    public String name() {
        return this.name;
    }

    public SocketBinding port(int port) {
        this.portExpression = "" + port;
        return this;
    }

    public SocketBinding port(String portExpression) {
        this.portExpression = portExpression;
        return this;
    }

    public String portExpression() {
        return this.portExpression;
    }

    public SocketBinding multicastAddress(String multicastAddress ) {
        this.multicastAddress = multicastAddress;
        return this;
    }

    public String multicastAddress() {
        return this.multicastAddress;
    }

    public SocketBinding multicastPort(int port) {
        this.multicastPortExpression = "" + port;
        return this;
    }

    public SocketBinding multicastPort(String port) {
        this.multicastPortExpression = port;
        return this;
    }

    public String multicastPortExpression() {
        return this.multicastPortExpression;
    }


}
