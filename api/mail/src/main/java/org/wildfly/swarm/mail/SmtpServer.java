package org.wildfly.swarm.mail;

/**
 * @author Ken Finnigan
 */
public class SmtpServer {

    private final String name;

    private String host;

    private String port;

    private String outboundSocketBindingRef = "mail-smtp";

    public SmtpServer() {
        this.name = "Default";
    }

    public SmtpServer(String name) {
        this.name = name;
    }

    public String name() {
        return this.name;
    }

    public SmtpServer host(String host) {
        this.host = host;
        return this;
    }

    public String host() {
        return this.host;
    }

    public SmtpServer port(String port) {
        this.port = port;
        return this;
    }

    public String port() {
        return this.port;
    }

    public SmtpServer outboundSocketBindingRef(String bindingReference) {
        this.outboundSocketBindingRef = bindingReference;
        return this;
    }

    public String outboundSocketBindingRef() {
        return this.outboundSocketBindingRef;
    }

    public String jndiName() {
        return "java:jboss/mail/" + this.name;
    }
}
