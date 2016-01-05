package org.wildfly.swarm.container;

/**
 * @author Bob McWhirter
 */
public class OutboundSocketBinding extends BaseSocketBinding {

    private String hostExpr;

    private String portExpr;

    public OutboundSocketBinding(String name) {
        super(name);
    }

    public OutboundSocketBinding remoteHost(String hostExpr) {
        this.hostExpr = hostExpr;
        return this;
    }

    public String remoteHostExpression() {
        return this.hostExpr;
    }

    public OutboundSocketBinding remotePort(int port) {
        this.portExpr = "" + port;
        return this;
    }

    public OutboundSocketBinding remotePort(String portExpr) {
        this.portExpr = portExpr;
        return this;
    }

    public String remotePortExpression() {
        return this.portExpr;
    }


}
